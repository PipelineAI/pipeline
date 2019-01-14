#!/usr/bin/env python3

import importlib
import os, subprocess
import logging
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
from tornado.options import define, options
from pipeline_monitor import prometheus_monitor as monitor
from pipeline_monitor import prometheus_monitor_registry as monitor_registry
from prometheus_client import CollectorRegistry, generate_latest, start_http_server, Summary, Counter, Histogram, Gauge

define('PIPELINE_NAME', default='', help='name', type=str)
define('PIPELINE_TAG', default='', help='tag', type=str)
define('PIPELINE_RUNTIME', default='', help='runtime', type=str)
define('PIPELINE_CHIP', default='', help='chip', type=str)
define('PIPELINE_RESOURCE_NAME', default='', help='resource name', type=str)
define('PIPELINE_RESOURCE_TAG', default='', help='resource tag', type=str)
define('PIPELINE_RESOURCE_TYPE', default='', help='resource type', type=str)
define('PIPELINE_RESOURCE_SUBTYPE', default='', help='resource subtype', type=str)

define('PIPELINE_RESOURCE_PATH', default='', help='model path', type=str)
define('PIPELINE_RESOURCE_SERVER_PORT', default='', help='tornado http server listen port', type=int)
define('PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_PORT', default='', help='port to run the prometheus http metrics server on', type=int)

# Create a metric to track time spent and requests made.
REQUEST_TIME_SUMMARY = Summary('request_processing_time', 'Model Server: Time Spent Processing Request', 
   ['method', 'name', 'tag', 'runtime', 'chip', 'resource_name', 'resource_tag', 'resource_type', 'resource_subtype'])

REQUESTS_IN_PROGRESS_GAUGE = Gauge('inprogress_requests', 'Model Server: Requests Currently In Progress', 
   ['method', 'name', 'tag', 'runtime', 'chip', 'resource_name', 'resource_tag', 'resource_type', 'resource_subtype'])

REQUEST_COUNTER = Counter('http_requests_total', 'Model Server: Total Http Request Count Since Last Process Restart', 
   ['method', 'name', 'tag', 'runtime', 'chip', 'resource_name', 'resource_tag', 'resource_type', 'resource_subtype'])

EXCEPTION_COUNTER = Counter('exceptions_total', 'Model Server: Total Exceptions', 
   ['method', 'name', 'tag', 'runtime', 'chip', 'resource_name', 'resource_tag', 'resource_type', 'resource_subtype'])


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


class StatusHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.write('{"status":"OK"}')
            self.finish()
        except Exception as e:
            _logger.exception('Status.get: Exception {0}'.format(str(e)))


class MetricsHandler(tornado.web.RequestHandler):

    def metrics(self):
        return generate_latest(monitor_registry)

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.write(self.metrics())
            self.finish()
        except Exception as e:
            _logger.exception('MetricsHandler.get: Exception {0}'.format(str(e)))


class ModelPredictPython3Handler(tornado.web.RequestHandler):

    _method = 'invoke'
    _key_list = [
        os.environ['PIPELINE_NAME'], 
	os.environ['PIPELINE_TAG'], 
	os.environ['PIPELINE_RUNTIME'], 
 	os.environ['PIPELINE_CHIP'], 
	os.environ['PIPELINE_RESOURCE_NAME'], 
	os.environ['PIPELINE_RESOURCE_TAG'], 
	os.environ['PIPELINE_RESOURCE_TYPE'], 
	os.environ['PIPELINE_RESOURCE_SUBTYPE']
    ]
    _key = '/'.join(_key_list)
    _runtime = os.environ['PIPELINE_RUNTIME']

    try:
        _invokable = importlib.import_module('pipeline_invoke_%s' % _runtime)
        _logger.info('Installing invoke bundle and updating environment: complete')
    except Exception as e:
        message = 'ModelPredictPython3Handler: Exception - {0} Error {1}'.format(_key, str(e))
        _logger.info(message)
        _logger.exception(message)

    @tornado.web.asynchronous
    def post(self):
        with EXCEPTION_COUNTER.labels(ModelPredictPython3Handler._method, *ModelPredictPython3Handler._key_list).count_exceptions(), \
          REQUESTS_IN_PROGRESS_GAUGE.labels(ModelPredictPython3Handler._method, *ModelPredictPython3Handler._key_list).track_inprogress():
            try:
                REQUEST_COUNTER.labels(ModelPredictPython3Handler._method, *ModelPredictPython3Handler._key_list).inc()

                # https://github.com/PipelineAI/product-private/issues/94
                # TODO:  the headers need to pass through the JVM layer, as well. 
                #        See PredictionService.scala for more TODO's.
                # TODO:  https://github.com/istio/istio.github.io/blob/a6fae1b3683e20e40a718f0bd2332350d8c7a87a/_docs/tasks/telemetry/distributed-tracing.md
                #incoming_headers = [ 'x-request-id',
                #                     'x-b3-traceid',
                #                     'x-b3-spanid',
                #                     'x-b3-parentspanid',
                #                     'x-b3-sampled',
                #                     'x-b3-flags',
                #                     'x-ot-span-context'
                #]
                #for ihdr in incoming_headers:
                #    val = self.request.headers.get(ihdr)
                #    if val is not None:
                #        headers[ihdr] = val
                
                response = ModelPredictPython3Handler._invokable.invoke(self.request.body)
            except Exception as e:
                response = 'ModelPredictPython3Handler.post: Exception - {0} Error {1}'.format(ModelPredictPython3Handler._key, str(e))
                _logger.info(response)
                _logger.exception(response)
            finally:
                self.write(response)
                self.finish()


class ModelServerApplication(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/ping', StatusHandler),
            (r'/metrics', MetricsHandler),
            (r'/', ModelPredictPython3Handler),
        ]
        tornado.web.Application.__init__(self, handlers, debug=False, autoreload=False)

    def fallback(self):
        _logger.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'

app = ModelServerApplication()

def main():
    try:
        tornado.options.parse_command_line()

        if not (
                    options.PIPELINE_NAME
                and options.PIPELINE_TAG
                and options.PIPELINE_RUNTIME
                and options.PIPELINE_CHIP
		and options.PIPELINE_RESOURCE_NAME
                and options.PIPELINE_RESOURCE_TAG
                and options.PIPELINE_RESOURCE_TYPE
                and options.PIPELINE_RESOURCE_SUBTYPE
                and options.PIPELINE_RESOURCE_PATH
                and options.PIPELINE_RESOURCE_SERVER_PORT
                and options.PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_PORT):
            _logger.error('--PIPELINE_NAME \
                           --PIPELINE_TAG \
                           --PIPELINE_RUNTIME \
                           --PIPELINE_CHIP \
                           --PIPELINE_RESOURCE_NAME \
                           --PIPELINE_RESOURCE_TAG \
                           --PIPELINE_RESOURCE_TYPE \
                           --PIPELINE_RESOURCE_SUBTYPE \
                           --PIPELINE_RESOURCE_PATH \
                           --PIPELINE_RESOURCE_SERVER_PORT \
                           --PIPELINE_RESOURCE_SERVER_TENSORFLOW_SERVING_PORT \
                           must be set')
            return

        _logger.info('Model Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_RESOURCE_SERVER_PORT))
        
        model_server_python_module_path = os.path.dirname(os.path.realpath(__file__))
        cmd = 'gunicorn --bind 0.0.0.0:%s --pythonpath %s -k tornado model_server_python:app' % (options.PIPELINE_RESOURCE_SERVER_PORT, model_server_python_module_path)
        subprocess.call(cmd, shell=True)
    except Exception as e:
        _logger.info('model_server_python.main: Exception {0}'.format(str(e)))
        _logger.exception('model_server_python.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
