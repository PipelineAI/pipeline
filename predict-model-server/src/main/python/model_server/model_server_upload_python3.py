#!/usr/bin/env python3

from io import StringIO
import logging
import os
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
import importlib.util
import tarfile
import subprocess
import cloudpickle as pickle
from tornado.options import define, options
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge
import json


define('PIPELINE_MODEL_STORE_PATH', default='', help='path to model_store', type=str)
define('PIPELINE_MODEL_TYPE', default='', help='prediction model type', type=str)
define('PIPELINE_MODEL_NAME', default='', help='prediction model name', type=str)
define('PIPELINE_MODEL_SERVER_PORT', default='', help='tornado http server listen port', type=int)
define('PIPELINE_MODEL_SERVER_PROMETHEUS_PORT', default=0, help='port to run the prometheus http metrics server on', type=int)
define('PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT', default='', help='port to run the prometheus http metrics server on', type=int)
define('PIPELINE_MODEL_SERVER_ALLOW_UPLOAD', default='True', help='allow /deploy', type=str)

UPLOAD_ARTIFACTS = ['pipeline.pkl', 'model.pkl', 'pipeline_package.pkl', 'pipeline.py', 'model.py']

# Create a metric to track time spent and requests made.
REQUEST_TIME_SUMMARY = Summary('request_processing_time', 'Model Server: Time Spent Processing Request', ['method', 'model_type', 'model_name'])
#REQUEST_TIME.observe(1.0)    # Observe 1.0 (seconds in this case)
REQUESTS_IN_PROGRESS_GAUGE = Gauge('inprogress_requests', 'Model Server: Requests Currently In Progress', ['method', 'model_type', 'model_name'])
REQUEST_COUNTER = Counter('http_requests_total', 'Model Server: Total Http Request Count Since Last Process Restart', ['method', 'model_type', 'model_name'])
EXCEPTION_COUNTER = Counter('exceptions_total', 'Model Server: Total Exceptions', ['method', 'model_type', 'model_name'])
#REQUEST_LATENCY_HISTOGRAM = Histogram('http_request_processing_time', 'model server: time spent processing requests.')
#REQUEST_LATENCY_BUCKETS_HISTROGRAM = Histogram('http_request_processing_time', 'model server: \
#                        histogram of time spent processing requests.', ['method', 'model_type', 'model_name'])

REGISTRY = CollectorRegistry()
REGISTRY.register(REQUEST_TIME_SUMMARY)
REGISTRY.register(REQUESTS_IN_PROGRESS_GAUGE)
REGISTRY.register(REQUEST_COUNTER)
REGISTRY.register(EXCEPTION_COUNTER)
#REGISTRY.register(REQUEST_LATENCY_HISTOGRAM)
#REGISTRY.register(REQUEST_LATENCY_BUCKETS_HISTOGRAM)

LOGGER = logging.getLogger(__name__)
LOGGER.setLevel(logging.ERROR)

CH = logging.StreamHandler()
CH.setLevel(logging.ERROR)
LOGGER.addHandler(CH)

TORNADO_ACCESS_LOGGER = logging.getLogger('tornado.access')
TORNADO_ACCESS_LOGGER.setLevel(logging.ERROR)

TORNADO_APPLICATION_LOGGER = logging.getLogger('tornado.application')
TORNADO_APPLICATION_LOGGER.setLevel(logging.ERROR)

TORNADO_GENERAL_LOGGER = logging.getLogger('tornado.general')
TORNADO_GENERAL_LOGGER.setLevel(logging.ERROR)

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/alive.txt', HealthCheckHandler),
            (r'/metrics', MetricsHandler),

            # /deploy is effectively disabled if PIPELINE_MODEL_SERVER_ALLOW_UPLOAD=False
            # url: /api/v1/model/deploy/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME
            (r'/api/v1/model/deploy/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelDeployPython3Handler),
            # url: /api/v1/model/predict/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME
            (r'/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelPredictPython3Handler),
        ]
        settings = dict(
            model_store_path=options.PIPELINE_MODEL_STORE_PATH,
            model_type=options.PIPELINE_MODEL_TYPE,
            model_name=options.PIPELINE_MODEL_NAME,
            model_server_port=options.PIPELINE_MODEL_SERVER_PORT,
            model_server_prometheus_server_port=options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT,
            model_server_allow_upload=options.PIPELINE_MODEL_SERVER_ALLOW_UPLOAD,
            model_server_tensorflow_serving_host='127.0.0.1',
            model_server_tensorflow_serving_port=options.PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT,
            request_timeout=120,
            debug=True,
            autoescape=None,
        )
        tornado.web.Application.__init__(self, handlers, **settings)

    def fallback(self):
        LOGGER.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'

class HealthCheckHandler(tornado.web.RequestHandler):

    def get(self):
        """Health check endpoint.
        ---
        get:
            description: Get health check
            responses:
               200:
                   description: health check to be returned
        """
        try:
            self.set_default_headers()
            self.set_status(200, None)
            self.write('ok')
            self.set_header('Content-Type', 'text/plain')
            self.flush()
        except Exception as e:
            logging.exception('HealthCheckHandler.get: Exception {0}'.format(str(e)))


class MetricsHandler(tornado.web.RequestHandler):

    def metrics(self):
        return generate_latest(REGISTRY)

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.write(self.metrics())
            self.finish()
        except Exception as e:
            logging.exception('MetricsHandler.get: Exception {0}'.format(str(e)))


class PioDeployException(Exception):
    """Deploy exceptions in this module."""
    def __init__(self, message):
        self.message = message


class ModelPredictPython3Handler(tornado.web.RequestHandler):

    _registry = {}


    @tornado.web.asynchronous
    def get(self, model_name):
        try:
            models = ModelPredictPython3Handler._registry
            self.write(models)
            self.finish()
        except Exception as e:
            logging.exception('ModelPredictPython3Handler.get: Exception {0}'.format(str(e)))


    @tornado.web.asynchronous
    def post(self, model_type, model_name):
        method = 'predict'
        model_key_list = [model_type, model_name]
        model_key = '/'.join(model_key_list)
        with EXCEPTION_COUNTER.labels(method, *model_key_list).count_exceptions(), \
          REQUESTS_IN_PROGRESS_GAUGE.labels(method, *model_key_list).track_inprogress():
            try:
                REQUEST_COUNTER.labels(method, *model_key_list).inc()
                model = self.get_model_assets(model_key_list)
                response = model.predict(self.request.body)
                self.write(response)
                self.finish()
            except Exception as e:
                message = 'ModelPredictPython3Handler.post: Exception - {0} Error {1}'.format(model_key, str(e))
                LOGGER.info(message)
                logging.exception(message)


    def get_model_assets(self, model_key_list, model_store_path):
        model_key = '/'.join(model_key_list)
        if model_key in ModelPredictPython3Handler._registry:
            model = ModelPredictPython3Handler._registry[model_key]
        else:
            LOGGER.info('Model Server get_model_assets if: begin')
            LOGGER.info('Installing model bundle and updating environment: begin')
            try:
                model_pkl_path = os.path.join(self.settings['model_store_path'],
                                              *model_key_list,
                                              '{0}'.format(UPLOAD_ARTIFACTS[1]))

                # Load pickled model from model directory
                with open(model_pkl_path, 'rb') as fh:
                    model = pickle.load(fh)

                #module_name = 'model'
                #spec = importlib.util.spec_from_file_location(module_name, module_path)
                #model = importlib.util.module_from_spec(spec)
                # Note:  This will initialize all global vars inside of pipeline.py
                #spec.loader.exec_module(model)

                ModelPredictPython3Handler._registry[model_key] = model
                LOGGER.info('Installing model bundle and updating environment: complete')
            except Exception as e:
                message = 'ModelPredictPython3Handler.get_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
                LOGGER.info(message)
                logging.exception(message)
                model = None
        return model


class ModelDeployPython3Handler(tornado.web.RequestHandler):

#    @REQUESTS_IN_PROGRESS.track_inprogress()
#    @REQUEST_LATENCY.time()
#    @EX_COUNT.count_exceptions()
#    @REQUEST_TIME.time()
    def post(self, model_type, model_name):
        method = 'deploy'
        model_key_list = [model_type, model_name]
        model_key = '/'.join(model_key_list)
        with EXCEPTION_COUNTER.labels(method, *model_key_list).count_exceptions(), \
          REQUESTS_IN_PROGRESS_GAUGE.labels(method, *model_key_list).track_inprogress():
            try:
                if not self.settings['model_server_allow_upload']:
                    raise PioDeployException('Deploy not allowed.')

                REQUEST_COUNTER.labels('deploy', *model_key_list).inc()
                fileinfo = self.request.files['file'][0]
                model_file_source_bundle_path = fileinfo['filename']
                (_, filename) = os.path.split(model_file_source_bundle_path)

                model_base_path = self.settings['model_store_path']
                model_base_path = os.path.expandvars(model_base_path)
                model_base_path = os.path.expanduser(model_base_path)
                model_base_path = os.path.abspath(model_base_path)

                bundle_path = os.path.join(model_base_path, *model_key_list)
                bundle_path_filename = os.path.join(bundle_path, filename)

                os.makedirs(bundle_path, exist_ok=True)
                with open(bundle_path_filename, 'wb+') as fh:
                    fh.write(fileinfo['body'])
                LOGGER.info('{0} uploaded {1}, saved as {2}'.format(str(self.request.remote_ip), str(filename),
                                                                bundle_path_filename))
                LOGGER.info('Extracting bundle {0} into {1}: begin'.format(filename, bundle_path))
                with tarfile.open(bundle_path_filename, 'r:gz') as tar:
                    tar.extractall(path=bundle_path)
                LOGGER.info('Extracting bundle {0} into {1}: complete'.format(filename, bundle_path))

                LOGGER.info('Updating dependencies: begin')
                completed_process = subprocess.run('cd {0} && [ -s ./requirements_conda.txt ] && conda install --yes --file \
                                               ./requirements_conda.txt'.format(bundle_path),
                                               timeout=1200,
                                               shell=True,
                                               stdout=subprocess.PIPE)
                completed_process = subprocess.run('cd {0} && [ -s ./requirements.txt ] && pip install -r \
                                               ./requirements.txt'.format(bundle_path),
                                               timeout=1200,
                                               shell=True,
                                               stdout=subprocess.PIPE)
                LOGGER.info('Updating dependencies: complete')

                # Update registry
                self.update_model_assets(model_key_list)

                LOGGER.info('"{0}" successfully deployed!'.format(model_key))
                self.write('"{0}" successfully deployed!'.format(model_key))
                #LOGGER.info('Model ID: {0}'.format(model_id))
                #self.write('Model ID: {0}'.format(model_id))
            except Exception as e:
                message = 'ModelDeployPython3Handler.post: Exception - {0} Error {1}'.format(model_key, str(e))
                LOGGER.info(message)
                logging.exception(message)


#    @REQUEST_TIME.time()
    def update_model_assets(self, model_key_list):
        model_key = '/'.join(model_key_list)
        LOGGER.info('Model Server update_model_assets if: begin')
        LOGGER.info('Installing model bundle and updating environment: begin')
        try:
            model_path = os.path.join(self.settings['model_store_path'],
                                      *model_key_list,
                                      '{0}'.format(UPLOAD_ARTIFACTS[1]))

            #model_module_name = 'model'
            #spec = importlib.util.spec_from_file_location(model_module_name, model_path)
            #pipeline = importlib.util.module_from_spec(spec)
            # Note:  This will initialize all global vars inside of pipeline.py
            #spec.loader.exec_module(model)

            # Load pickled model from model directory
            with open(model_path, 'rb') as fh:
                model = pickle.load(fh)

            ModelPredictPython3Handler._registry[model_key] = model
            LOGGER.info('Installing model bundle and updating environment: complete')
        except Exception as e:
            message = 'ModelPredictPython3Handler.update_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
            LOGGER.info(message)
            logging.exception(message)
            model = None
        return model


def main():
    try:
        tornado.options.parse_command_line()
        if not (options.PIPELINE_MODEL_STOREPATH_
                and options.PIPELINE_MODEL_TYPE
                and options.PIPELINE_MODEL_NAME
                and options.PIPELINE_MODEL_SERVER_PORT
                and options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT
                and options.PIPELINE_MODEL_SERVER_ALLOW_UPLOAD
                and options.PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT):
            LOGGER.error('--PIPELINE_MODEL_STORE_PATH and --PIPELINE_MODEL_TYPE and --PIPELINE_MODEL_NAME and \
                          --PIPELINE_MODEL_SERVER_PORT and --PIPELINE_MODEL_SERVER_PROMETHEUS_PORT and \
                          --PIPELINE_MODEL_SERVER_ALLOW_UPLOAD and --PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT and must be set')
            return

        LOGGER.info('Model Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIPELINE_MODEL_SERVER_PORT)
        LOGGER.info('Model Server main: complete start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))

        LOGGER.info('Prometheus Server main: begin start prometheus http server port {0}'.format(
            options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))
        # Start up a web server to expose request made and time spent metrics to Prometheus
        # TODO potentially expose metrics to Prometheus using the Tornado HTTP server as long as their not blocking
        # see the MetricsHandler class which provides a BaseHTTPRequestHandler
        # https://github.com/prometheus/client_python/blob/ce5542bd8be2944a1898e9ac3d6e112662153ea4/prometheus_client/exposition.py#L79
        start_http_server(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT)
        LOGGER.info('Prometheus Server main: complete start prometheus http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))

        tornado.ioloop.IOLoop.current().start()
        print('...Python-based Model Server Started!')
    except Exception as e:
        LOGGER.info('model_server_python3.main: Exception {0}'.format(str(e)))
        logging.exception('model_server_python3.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()

