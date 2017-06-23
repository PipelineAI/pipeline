#!/usr/bin/env python3

from io import StringIO
import logging
import os
import tornado.auth
import tornado.escape
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
import tornado.httputil
import importlib.util
import tarfile
import subprocess
import dill as pickle
from tornado.options import define, options
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

from grpc.beta import implementations
import asyncio
import tensorflow as tf
import predict_pb2
import prediction_service_pb2

define('PIO_MODEL_STORE_HOME', default='', help='path to model_store', type=str)
define('PIO_MODEL_TYPE', default='', help='prediction model type', type=str)
define('PIO_MODEL_NAMESPACE', default='', help='prediction model namespace', type=str)
define('PIO_MODEL_NAME', default='', help='prediction model name', type=str)
define('PIO_MODEL_VERSION', default='', help='prediction model version', type=str)
define('PIO_MODEL_SERVER_PORT', default='9876', help='tornado http server listen port', type=int)
define('PIO_MODEL_SERVER_PROMETHEUS_PORT', default=8080, help='port to run the prometheus http metrics server on', type=int)
define('PIO_MODEL_SERVER_TENSORFLOW_SERVING_PORT', default='9000', help='port to run the prometheus http metrics server on', type=int)

MODEL_MODULE_NAME = 'pio_bundle'
# Create a metric to track time spent and requests made.
REQUEST_TIME = Summary('request_processing_seconds', 'Model Server: Time spent processing request')
REQUEST_TIME.observe(1.0)    # Observe 1.0 (seconds in this case)
REQUESTS_IN_PROGRESS = Gauge('inprogress_requests', 'model server: request current in progress')
REQUESTS_COUNT = Counter('http_requests_total', 'model server: total \
            http request count since the last time the process was restarted', ['method', 'model_type', 'model_namespace',
                                                                                'model_name', 'model_version'])
EX_COUNT = Counter('exceptions_total', 'model server: total http request count since the last time the process was restarted')
REQUEST_LATENCY = Histogram('http_request_processing_seconds', 'model server: time in seconds spent processing requests.')
REQUEST_LATENCY_BUCKETS = Histogram('http_request_duration_microseconds', 'model server: \
                         time in microseconds spent processing requests.', ['method', 'model_type', 'model_namespace',
                                                                            'model_name', 'model_version'])
REGISTRY = CollectorRegistry()
REGISTRY.register(REQUEST_TIME)
REGISTRY.register(REQUESTS_IN_PROGRESS)
REGISTRY.register(REQUESTS_COUNT)
REGISTRY.register(EX_COUNT)
REGISTRY.register(REQUEST_LATENCY)
REGISTRY.register(REQUEST_LATENCY_BUCKETS)
LOGGER = logging.getLogger(__name__)
LOGGER.setLevel(logging.DEBUG)
CH = logging.StreamHandler()
CH.setLevel(logging.DEBUG)
LOGGER.addHandler(CH)

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/', IndexHandler),
            (r'/healthz', HealthzHandler),
            (r'/metrics', MetricsHandler),
            # TODO:  Disable DEPLOY if we're not explicitly in PIO_MODEL_ENVIRONMENT=dev mode (or equivalent)
            # url: /api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r'/api/v1/model/deploy/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelDeployTensorFlowHandler),
            # url: /api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r'/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelPredictTensorFlowHandler),
        ]
        settings = dict(
            model_store_path=options.PIO_MODEL_STORE_HOME,
            model_type=options.PIO_MODEL_TYPE,
            model_namespace=options.PIO_MODEL_NAMESPACE,
            model_name=options.PIO_MODEL_NAME,
            model_version=options.PIO_MODEL_VERSION,
            model_server_port=options.PIO_MODEL_SERVER_PORT,
            model_server_prometheus_server_port=options.PIO_MODEL_SERVER_PROMETHEUS_PORT,
            model_server_tensorflow_serving_host='127.0.0.1',
            model_server_tensorflow_serving_port=options.PIO_MODEL_SERVER_TENSORFLOW_SERVING_PORT,
            template_path=os.path.join(os.path.dirname(__file__), 'templates'),
            static_path=os.path.join(os.path.dirname(__file__), 'static',
            request_timeout=120,
            debug=True,
            autoescape=None,
        )
        tornado.web.Application.__init__(self, handlers, **settings)


    def fallback(self):
        LOGGER.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'


class IndexHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        self.render('index.html')


class HealthzHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.finish()
        except Exception as e:
            logging.exception('HealthzHandler.get: Exception {0}'.format(str(e)))


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


class ModelPredictTensorFlowHandler(tornado.web.RequestHandler):
    _registry = {}

    @tornado.web.asynchronous
    def get(self, model_type, model_namespace, model_name, model_version):
        try:
            self.render('index.html')
        except Exception as e:
            logging.exception('MainHandler.get: Exception {0}'.format(str(e)))

    @REQUESTS_IN_PROGRESS.track_inprogress()
    @REQUEST_LATENCY.time()
    @EX_COUNT.count_exceptions()
    @REQUEST_TIME.time()
    @tornado.web.asynchronous
    def post(self, model_type, model_namespace, model_name, model_version):
        model_key_list = [model_type, model_namespace, model_name, model_version]

        try:
            REQUESTS_COUNT.labels('predict', *model_key_list).inc()
            model = self.get_model_assets(model_key_list)
            with REQUEST_LATENCY_BUCKETS.labels('predict', *model_key_list).time():
                # TODO:  Reuse instead of creating this channel everytime
                channel = implementations.insecure_channel(self.settings['model_server_tensorflow_serving_host'],
                                                   int(self.settings['model_server_tensorflow_serving_port']))
                stub = prediction_service_pb2.beta_create_PredictionService_stub(channel)

                # Transform raw inputs to TensorFlow PredictRequest
                transformed_inputs_request = model.transform_request(self.request.body)
                inputs_tensor_proto = tf.make_tensor_proto(transformed_inputs_request,
                                                   dtype=tf.float32)
                tf_request = predict_pb2.PredictRequest()
                tf_request.inputs['x_observed'].CopyFrom(inputs_tensor_proto)

                tf_request.model_spec.name = model_name
                tf_request.model_spec.version.value = int(model_version)

                # Transform TensorFlow PredictResponse into output
                response = stub.Predict(tf_request, self.settings['request_timeout'])
                response_np = tf.contrib.util.make_ndarray(response.outputs['y_pred'])

                transformed_response_np = model.transform_response(response_np)
                self.write(transformed_response_np)
            self.finish()
        except Exception as e:
            message = 'MainHandler.post: Exception - {0} Error {1}'.format('|_|'.join(model_key_list), str(e))
            LOGGER.info(message)
            logging.exception(message)


    @REQUEST_TIME.time()
    def get_model_assets(self, model_key_list):
        model_key = '|_|'.join(model_key_list)
        if model_key in ModelPredictTensorFlowHandler._registry:
            model = ModelPredictTensorFlowHandler._registry[model_key]
        else:
            LOGGER.info('Model Server get_model_assets if: begin')
            with REQUEST_LATENCY_BUCKETS.labels('pickle-load', *model_key_list).time():
                LOGGER.info('Installing bundle and updating environment: begin')
                try:
                    model_pkl_path = os.path.join(self.settings['model_store_path'], *model_key_list,
                                                     '{0}.pkl'.format(MODEL_MODULE_NAME))

                    # Load pickled model from model directory
                    with open(model_pkl_path, 'rb') as fh:
                        model = pickle.load(fh)

                    ModelPredictTensorFlowHandler._registry[model_key] = model
                    LOGGER.info('Installing bundle and updating environment: complete')
                except Exception as e:
                    message = 'ModelPredictTensorFlowHandler.get_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
                    LOGGER.info(message)
                    logging.exception(message)
                    model = None
        return model

class ModelDeployTensorFlowHandler(tornado.web.RequestHandler):

    @REQUESTS_IN_PROGRESS.track_inprogress()
    @REQUEST_LATENCY.time()
    @EX_COUNT.count_exceptions()
    @REQUEST_TIME.time()
    def post(self, model_type, model_namespace, model_name, model_version):
        model_key_list = [model_type, model_namespace, model_name, model_version]
        model_id = '|_|'.join(model_key_list)
        try:
            REQUESTS_COUNT.labels('deploy', *model_key_list).inc()
            with REQUEST_LATENCY_BUCKETS.labels('deploy', *model_key_list).time():
                fileinfo = self.request.files['file'][0]
                model_file_source_bundle_path = fileinfo['filename']
                (_, filename) = os.path.split(model_file_source_bundle_path)

                model_base_path = self.settings['model_store_path']
                model_base_path = os.path.expandvars(model_base_path)
                model_base_path = os.path.expanduser(model_base_path)
                model_base_path = os.path.abspath(model_base_path)

                bundle_path = os.path.join(model_base_path, model_type, model_namespace, model_name, model_version)
                bundle_path_filename = os.path.join(bundle_path, filename)

                os.makedirs(bundle_path, exist_ok=True)
                with open(bundle_path_filename, 'wb+') as fh:
                    fh.write(fileinfo['body'])
                LOGGER.info('{0} uploaded {1}, saved as {2}'.format(str(self.request.remote_ip), str(filename),
                                                                    bundle_path_filename))
                LOGGER.info('Uploaded and extracting bundle {0} into {1}: begin'.format(filename, bundle_path))
                with tarfile.open(bundle_path_filename, 'r:gz') as tar:
                    tar.extractall(path=bundle_path)
                LOGGER.info('Uploaded and extracting bundle {0} into {1}: complete'.format(filename, bundle_path))

                LOGGER.info('Installing bundle and updating environment: begin')
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
                LOGGER.info('Installing bundle and updating environment: complete')
            LOGGER.info('{0} Model successfully deployed!'.format(model_id))
            self.write('{0} Model successfully deployed!'.format(model_id))
        except Exception as e:
            message = 'ModelDeployTensorFlowHandler.post: Exception - {0} Error {1}'.format(model_id, str(e))
            LOGGER.info(message)
            logging.exception(message)


def main():
    try:
        tornado.options.parse_command_line()
        if not (options.PIO_MODEL_STORE_HOME and options.PIO_MODEL_TYPE and options.PIO_MODEL_NAMESPACE
                and options.PIO_MODEL_NAME and options.PIO_MODEL_VERSION and options.PIO_MODEL_SERVER_PORT
                and options.PIO_MODEL_SERVER_PROMETHEUS_PORT and options.PIO_MODEL_SERVER_TENSORFLOW_SERVING_PORT):
            print('--PIO_MODEL_STORE_HOME and --PIO_MODEL_TYPE and --PIO_MODEL_NAMESPACE and --PIO_MODEL_NAME and \
                  --PIO_MODEL_VERSION and --PIO_MODEL_SERVER_PORT and --PIO_MODEL_SERVER_PROMETHEUS_PORT and \
                  --PIO_MODEL_SERVER_TENSORFLOW_SERVING_PORT must be set')
            return

        LOGGER.info('Model Server main: begin start tornado-based http server port {0}'.format(options.PIO_MODEL_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIO_MODEL_SERVER_PORT)
        LOGGER.info('Model Server main: complete start tornado-based http server port {0}'.format(options.PIO_MODEL_SERVER_PORT))

        LOGGER.info('Prometheus Server main: begin start prometheus http server port {0}'.format(
            options.PIO_MODEL_SERVER_PROMETHEUS_PORT))
        # Start up a web server to expose request made and time spent metrics to Prometheus
        # TODO potentially expose metrics to Prometheus using the Tornado HTTP server as long as their not blocking
        # see the MetricsHandler class which provides a BaseHTTPRequestHandler
        # https://github.com/prometheus/client_python/blob/ce5542bd8be2944a1898e9ac3d6e112662153ea4/prometheus_client/exposition.py#L79
        start_http_server(options.PIO_MODEL_SERVER_PROMETHEUS_PORT)
        LOGGER.info('Prometheus Server main: complete start prometheus http server port {0}'.format(options.PIO_MODEL_SERVER_PROMETHEUS_PORT))

        tornado.ioloop.IOLoop.current().start()
    except Exception as e:
        LOGGER.info('model_server_python3.main: Exception {0}'.format(str(e)))
        logging.exception('model_server_python3.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
