#!/usr/bin/env python3

import importlib
import os
import logging
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
import cloudpickle as pickle
from tornado.options import define, options
from pipeline_monitors import monitor, monitor_registry 
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

define('PIPELINE_MODEL_TYPE', default='', help='model type', type=str)
define('PIPELINE_MODEL_NAME', default='', help='model name', type=str)
define('PIPELINE_MODEL_PATH', default='', help='model path', type=str)
define('PIPELINE_MODEL_SERVER_PORT', default='', help='tornado http server listen port', type=int)
define('PIPELINE_MODEL_SERVER_PROMETHEUS_PORT', default=0, help='port to run the prometheus http metrics server on', type=int)
define('PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT', default='', help='port to run the prometheus http metrics server on', type=int)

# Create a metric to track time spent and requests made.
REQUEST_TIME_SUMMARY = Summary('request_processing_time', 'Model Server: Time Spent Processing Request', ['method', 'model_type', 'model_name'])
REQUESTS_IN_PROGRESS_GAUGE = Gauge('inprogress_requests', 'Model Server: Requests Currently In Progress', ['method', 'model_type', 'model_name'])
REQUEST_COUNTER = Counter('http_requests_total', 'Model Server: Total Http Request Count Since Last Process Restart', ['method', 'model_type', 'model_name'])
EXCEPTION_COUNTER = Counter('exceptions_total', 'Model Server: Total Exceptions', ['method', 'model_type', 'model_name'])

monitor_registry.register(REQUEST_TIME_SUMMARY)
monitor_registry.register(REQUESTS_IN_PROGRESS_GAUGE)
monitor_registry.register(REQUEST_COUNTER)
monitor_registry.register(EXCEPTION_COUNTER)

_logger = logging.getLogger(__name__)
_logger .setLevel(logging.INFO)

_stream_handler = logging.StreamHandler()
_stream_handler.setLevel(logging.INFO)
_logger.addHandler(_stream_handler)

TORNADO_ACCESS_LOGGER = logging.getLogger('tornado.access')
TORNADO_ACCESS_LOGGER.setLevel(logging.ERROR)

TORNADO_APPLICATION_LOGGER = logging.getLogger('tornado.application')
TORNADO_APPLICATION_LOGGER.setLevel(logging.ERROR)

TORNADO_GENERAL_LOGGER = logging.getLogger('tornado.general')
TORNADO_GENERAL_LOGGER.setLevel(logging.ERROR)

class Application(tornado.web.Application):
    def __init__(self):
        settings = dict(
            model_type=options.PIPELINE_MODEL_TYPE,
            model_name=options.PIPELINE_MODEL_NAME,
            model_path=options.PIPELINE_MODEL_PATH,
            model_server_port=options.PIPELINE_MODEL_SERVER_PORT,
            model_server_prometheus_server_port=options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT,
            model_server_tensorflow_serving_host='127.0.0.1',
            model_server_tensorflow_serving_port=options.PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT,
            request_timeout=120,
            debug=True,
            autoescape=None,
        )
        handlers = [
            (r'/healthz', HealthzHandler),
            (r'/metrics', MetricsHandler),
            (r'/environment', EnvironmentHandler),
            # url: /api/v1/model/predict/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME
            (r'/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelPredictPython3Handler, dict(model_path=settings['model_path'])),
        ]
        tornado.web.Application.__init__(self, handlers, **settings)

    def fallback(self):
        _logger.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'


class HealthzHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.finish()
        except Exception as e:
            _logger.exception('HealthzHandler.get: Exception {0}'.format(str(e)))


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
            _logger.exception('MetricsHandler.get: Exception {0}'.format(str(e)))


class EnvironmentHandler(tornado.web.RequestHandler):
    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            cmd = 'conda list'
            process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE)
            (output, error) = process.communicate()
            output = output.rstrip().decode('utf-8')
            self.write(output)
            self.finish()
        except Exception as e:
            _logger.exception('EnvironmentHandler.get: Exception {0}'.format(str(e)))


class ModelPredictPython3Handler(tornado.web.RequestHandler):

    _registry = {}

    def initialize(self, model_path):
        self.model_path = model_path

    @tornado.web.asynchronous
    def get(self, model_name):
        try:
            models = ModelPredictPython3Handler._registry
            self.write(models)
            self.finish()
        except Exception as e:
            _logger.exception('ModelPredictPython3Handler.get: Exception {0}'.format(str(e)))


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
                _logger.info(message)
                _logger.exception(message)


    def get_model_assets(self, model_key_list):
        model_key = '/'.join(model_key_list)
        if model_key in ModelPredictPython3Handler._registry:
            model = ModelPredictPython3Handler._registry[model_key]
        else:
            _logger.info('Model Server get_model_assets if: begin')
            _logger.info('Installing model bundle and updating environment: begin')
            try:
                artifact_name = 'pipeline_predict'
                spec = importlib.util.spec_from_file_location(artifact_name, '%s/pipeline_predict.py' % self.model_path) 
                model = importlib.util.module_from_spec(spec)
                # Note:  This will initialize all global vars inside of model
                spec.loader.exec_module(model)

                ModelPredictPython3Handler._registry[model_key] = model 
                _logger.info('Installing model bundle and updating environment: complete')
            except Exception as e:
                message = 'ModelPredictPython3Handler.get_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
                _logger.info(message)
                _logger.exception(message)
                model = None
        return model 


def main():
    try:
        tornado.options.parse_command_line()
        if not (options.PIPELINE_MODEL_TYPE
                and options.PIPELINE_MODEL_NAME 
                and options.PIPELINE_MODEL_PATH
                and options.PIPELINE_MODEL_SERVER_PORT
                and options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT 
                and options.PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT):
            _logger.error('--PIPELINE_MODEL_TYPE and --PIPELINE_MODEL_NAME and --PIPELINE_MODEL_PATH \
                          --PIPELINE_MODEL_SERVER_PORT and --PIPELINE_MODEL_SERVER_PROMETHEUS_PORT and \
                          --PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT and must be set')
            return

        _logger.info('Model Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIPELINE_MODEL_SERVER_PORT)
        _logger.info('Model Server main: complete start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))

        _logger.info('Prometheus Server main: begin start prometheus http server port {0}'.format(
            options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))
        start_http_server(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT)
        _logger.info('Prometheus Server main: complete start prometheus http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))

        tornado.ioloop.IOLoop.current().start()
        _logger.info('...Python-based Model Server Started!')
    except Exception as e:
        _logger.info('model_server_python.main: Exception {0}'.format(str(e)))
        _logger.exception('model_server_python.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
