#!/usr/bin/env python3

import importlib
import os
import logging
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
from tornado.options import define, options
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

define('PIPELINE_MODEL_TYPE', default='', help='prediction model type', type=str)
define('PIPELINE_MODEL_NAME', default='', help='prediction model name', type=str)
define('PIPELINE_MODEL_VERSION', default='', help='prediction model version', type=str)
define('PIPELINE_MODEL_PATH', default='', help='prediction model path', type=str)
define('PIPELINE_MODEL_SERVER_PORT', default='', help='tornado http server listen port', type=int)
define('PIPELINE_MODEL_SERVER_PROMETHEUS_PORT', default=0, help='port to run the prometheus http metrics server on', type=int)

# Create a metric to track time spent and requests made.
REQUEST_TIME_SUMMARY = Summary('request_processing_time', 'Model Server: Time Spent Processing Request', ['method', 'model_type', 'model_name'])
REQUESTS_IN_PROGRESS_GAUGE = Gauge('inprogress_requests', 'Model Server: Requests Currently In Progress', ['method', 'model_type', 'model_name'])
REQUEST_COUNTER = Counter('http_requests_total', 'Model Server: Total Http Request Count Since Last Process Restart', ['method', 'model_type', 'model_name'])
EXCEPTION_COUNTER = Counter('exceptions_total', 'Model Server: Total Exceptions', ['method', 'model_type', 'model_name'])

REGISTRY = CollectorRegistry()
REGISTRY.register(REQUEST_TIME_SUMMARY)
REGISTRY.register(REQUESTS_IN_PROGRESS_GAUGE)
REGISTRY.register(REQUEST_COUNTER)
REGISTRY.register(EXCEPTION_COUNTER)

LOGGER = logging.getLogger(__name__)
LOGGER.setLevel(logging.DEBUG)

CH = logging.StreamHandler()
CH.setLevel(logging.DEBUG)
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
            (r'/alive.txt', HealthzHandler),
            (r'/healthz', HealthzHandler),
            (r'/metrics', MetricsHandler),
            # url: /api/v1/model/predict/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME
            (r'/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelPredictPython3Handler),
        ]
        settings = dict(
            model_type=options.PIPELINE_MODEL_TYPE,
            model_name=options.PIPELINE_MODEL_NAME,
            model_version=options.PIPELINE_MODEL_VERSION,
            model_path=options.PIPELINE_MODEL_PATH,
            model_server_port=options.PIPELINE_MODEL_SERVER_PORT,
            model_server_prometheus_server_port=options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT,
            model_server_tensorflow_serving_host='127.0.0.1',
            request_timeout=120,
            debug=True,
            autoescape=None,
        )
        tornado.web.Application.__init__(self, handlers, **settings)

    def fallback(self):
        LOGGER.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'


class HealthzHandler(tornado.web.RequestHandler):

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


    def get_model_assets(self, model_key_list):
        model_key = '/'.join(model_key_list)
        if model_key in ModelPredictPython3Handler._registry:
            model = ModelPredictPython3Handler._registry[model_key]
        else:
            LOGGER.info('Model Server get_model_assets if: begin')
            LOGGER.info('Installing model bundle and updating environment: begin')
            try:
                artifact_name = 'pipeline_model'
                spec = importlib.util.spec_from_file_location(artifact_name,
                    '{0}/pipeline_model.py'.format(self.settings['model_path']))
                model = importlib.util.module_from_spec(spec)
                # Note:  This will initialize all global vars inside of model
                spec.loader.exec_module(model)

                ModelPredictPython3Handler._registry[model_key] = model
                LOGGER.info('Installing model bundle and updating environment: complete')
            except Exception as e:
                message = 'ModelPredictPython3Handler.get_model_assets: Exception - {0} Error {1}'.format(model_key, str(e))
                LOGGER.info(message)
                logging.exception(message)
                model = None
        return model


def main():
    try:
        tornado.options.parse_command_line()
        if not (options.PIPELINE_MODEL_TYPE
                and options.PIPELINE_MODEL_NAME
                and options.PIPELINE_MODEL_VERSION
                and options.PIPELINE_MODEL_PATH
                and options.PIPELINE_MODEL_SERVER_PORT
                and options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT):
            LOGGER.error('--PIPELINE_MODEL_TYPE \
                        and --PIPELINE_MODEL_NAME \
                        and --PIPELINE_MODEL_VERSION \
                        and --PIPELINE_MODEL_PATH \
                        and --PIPELINE_MODEL_SERVER_PORT \
                        and --PIPELINE_MODEL_SERVER_PROMETHEUS_PORT \
                        and must be set')
            return

        LOGGER.info('Model Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIPELINE_MODEL_SERVER_PORT)
        LOGGER.info('Model Server main: complete start tornado-based http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PORT))

        LOGGER.info('Prometheus Server main: begin start prometheus http server port {0}'.format(
            options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))
        start_http_server(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT)
        LOGGER.info('Prometheus Server main: complete start prometheus http server port {0}'.format(options.PIPELINE_MODEL_SERVER_PROMETHEUS_PORT))

        tornado.ioloop.IOLoop.current().start()
        print('...Python-based Model Server Started!')
    except Exception as e:
        LOGGER.info('model_server_python3.main: Exception {0}'.format(str(e)))
        logging.exception('model_server_python3.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
