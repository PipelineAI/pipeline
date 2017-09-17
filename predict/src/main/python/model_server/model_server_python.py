#!/usr/bin/env python3

import importlib
import os
import logging
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
from tornado.options import define, options
from pipeline_monitors import prometheus_monitor as monitor
from pipeline_monitors import prometheus_monitor_registry as monitor_registry
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

define('PIPELINE_MODEL_TYPE', default='', help='model type', type=str)
define('PIPELINE_MODEL_NAME', default='', help='model name', type=str)
define('PIPELINE_MODEL_TAG', default='', help='model tag', type=str)
define('PIPELINE_MODEL_PATH', default='', help='model path', type=str)
define('PIPELINE_MODEL_SERVER_PORT', default='', help='tornado http server listen port', type=int)
define('PIPELINE_MODEL_SERVER_PROMETHEUS_PORT', default=0, help='port to run the prometheus http metrics server on', type=int)
define('PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT', default='', help='port to run the prometheus http metrics server on', type=int)

# Create a metric to track time spent and requests made.
REQUEST_TIME_SUMMARY = Summary('request_processing_time', 'Model Server: Time Spent Processing Request', ['method', 'model_type', 'model_name', 'model_tag'])
REQUESTS_IN_PROGRESS_GAUGE = Gauge('inprogress_requests', 'Model Server: Requests Currently In Progress', ['method', 'model_type', 'model_name', 'model_tag'])
REQUEST_COUNTER = Counter('http_requests_total', 'Model Server: Total Http Request Count Since Last Process Restart', ['method', 'model_type', 'model_name', 'model_tag'])
EXCEPTION_COUNTER = Counter('exceptions_total', 'Model Server: Total Exceptions', ['method', 'model_type', 'model_name', 'model_tag'])

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
    def get(self, model_name, model_tag):
        try:
            models = ModelPredictPython3Handler._registry
            self.write(models)
            self.finish()
        except Exception as e:
            _logger.exception('ModelPredictPython3Handler.get: Exception {0}'.format(str(e)))


    @tornado.web.asynchronous
    def post(self, model_type, model_name, model_tag):
        method = 'predict'
        model_key_list = [model_type, model_name, model_tag]
        model_key = '/'.join(model_key_list)
        with EXCEPTION_COUNTER.labels(method, *model_key_list).count_exceptions(), \
          REQUESTS_IN_PROGRESS_GAUGE.labels(method, *model_key_list).track_inprogress():
            try:
                REQUEST_COUNTER.labels(method, *model_key_list).inc()
                model = self.get_model_assets(model_key_list)
                response = model.predict(self.request.body)
            except Exception as e:
                response = 'ModelPredictPython3Handler.post: Exception - {0} Error {1}'.format(model_key, str(e))
                _logger.info(response)
                _logger.exception(response)
            finally:
                self.write(response)
                self.finish()


    def get_model_assets(self, model_key_list):
        model_key = '/'.join(model_key_list)
        if model_key not in ModelPredictPython3Handler._registry:
            _logger.info('Model Server get_model_assets if: begin')
            _logger.info('Installing model bundle and updating environment: begin')
            try:
                # Custom Import from $PIPELINE_MODEL_PATH/pipeline_predict.py
                import pipeline_predict
                ModelPredictPython3Handler._registry[model_key] = pipeline_predict
                _logger.info('Installing model bundle and updating environment: complete')
            except Exception as e:
                message = 'ModelPredictPython3Handler.get_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
                _logger.info(message)
                _logger.exception(message)
                ModelPredictPython3Handler._registry[model_key] = None

        return ModelPredictPython3Handler._registry[model_key]


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/healthz', HealthzHandler),
            (r'/metrics', MetricsHandler),
            (r'/environment', EnvironmentHandler),
            # url: /api/v1/model/predict/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME/$PIPELINE_MODEL_TAG
            (r'/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelPredictPython3Handler, dict(model_path=options.PIPELINE_MODEL_PATH)),
        ]
        tornado.web.Application.__init__(self, handlers)

    def fallback(self):
        _logger.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'


def main():
    try:
        tornado.options.parse_command_line()
        if not (options.PIPELINE_MODEL_TYPE
                and options.PIPELINE_MODEL_NAME
                and options.PIPELINE_MODEL_TAG
                and options.PIPELINE_MODEL_PATH
                and options.PIPELINE_MODEL_SERVER_PORT
                and options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT
                and options.PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT):
            _logger.error('--PIPELINE_MODEL_TYPE \
                           --PIPELINE_MODEL_NAME \
                           --PIPELINE_MODEL_TAG \
                           --PIPELINE_MODEL_PATH \
                           --PIPELINE_MODEL_SERVER_PORT \
                           --PIPELINE_MODEL_SERVER_PROMETHEUS_PORT \
                           --PIPELINE_MODEL_SERVER_TENSORFLOW_SERVING_PORT \
                           must be set')
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
