#!/usr/bin/env python3

import os
import sys
import tornado.auth
import tornado.escape
import tornado.ioloop
#import tornado.options
import tornado.web
import tornado.httpserver
import tornado.httputil
#import tornado.gen
import dill as pickle
import fnmatch
import importlib.util
import tarfile
import subprocess
import logging
from tornado.options import define, options
from prometheus_client import start_http_server, Summary

define("PIO_MODEL_STORE_HOME", default="model_store", help="path to model_store", type=str)
define("PIO_MODEL_TYPE", default="python3", help="prediction model type", type=str)
define("PIO_MODEL_NAMESPACE", default="default", help="prediction model namespace", type=str)
define("PIO_MODEL_NAME", default="scikit_balancescale", help="prediction model name", type=str)
define("PIO_MODEL_VERSION", default="v0", help="prediction model version", type=str)
define("PIO_MODEL_SERVER_PORT", default="9876", help="tornado http server listen port", type=int)
define("PIO_MODEL_SERVER_PROMETHEUS_PORT", default="8080", help="port to run the prometheus http metrics server on", type=int)

# Create a metric to track time spent and requests made.
REQUEST_TIME = Summary('request_processing_seconds', 'Model Server: Time spent processing request')
REQUEST_TIME.observe(1.0)    # Observe 1.0 (seconds in this case)
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)
logger.addHandler(ch)

class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r"/", IndexHandler),
            # url: /v1/model/predict/python3/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r"/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelPredictPython3Handler),
            # TODO:  Disable this if we're not explicitly in PIO_MODEL_ENVIRONMENT=dev mode
            # url: /v1/model/deploy/python3/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r"/v1/model/deploy/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelDeployPython3Handler),
        ]
        settings = dict(
            model_store_path=options.PIO_MODEL_STORE_HOME,
            model_type=options.PIO_MODEL_TYPE,
            model_namespace=options.PIO_MODEL_NAMESPACE,
            model_name=options.PIO_MODEL_NAME,
            model_version=options.PIO_MODEL_VERSION,
            model_server_port=options.PIO_MODEL_SERVER_PORT,
            model_server_prometheus_server_port=options.PIO_MODEL_SERVER_PROMETHEUS_PORT,
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            static_path=os.path.join(os.path.dirname(__file__), "static"),
            debug=True,
            autoescape=None,
        )
        tornado.web.Application.__init__(self, handlers, **settings)

    def fallback(self):
        logger.warn('Model Server Application fallback: %s' % self)
        return 'fallback!'


class IndexHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        self.render("index.html")


class ModelPredictPython3Handler(tornado.web.RequestHandler):
    registry = {}

    @REQUEST_TIME.time()
    @tornado.web.asynchronous
    def post(self, model_type, model_namespace, model_name, model_version):
        (_, model_key, model, transformers_module) = self.get_model_assets(model_type,
                                                                           model_namespace,
                                                                           model_name,
                                                                           model_version)

        transformed_inputs = transformers_module.transform_inputs(self.request.body)
        outputs = model.predict(transformed_inputs)
        transformed_outputs = transformers_module.transform_outputs(outputs)
        self.write(transformed_outputs)
        self.finish()


    @REQUEST_TIME.time()
    def get_model_assets(self, model_type, model_namespace, model_name, model_version):
        model_key = '%s_%s_%s_%s' % (model_type, model_namespace, model_name, model_version)
        if model_key in self.registry:
            (model_file_absolute_path, model_key, model, transformers_module) = self.registry[model_key]
        else:
            model_base_path = os.path.join(self.settings['model_store_path'], model_type)
            model_base_path = os.path.join(model_base_path, model_namespace)
            model_base_path = os.path.join(model_base_path, model_name)
            model_base_path = os.path.join(model_base_path, model_version)

            model_filename = fnmatch.filter(os.listdir(model_base_path), "*.pkl")[0]

            # Set absolute path to model directory
            model_file_absolute_path = os.path.join(model_base_path, model_filename)

            # Load pickled model from model directory
            with open(model_file_absolute_path, 'rb') as model_file:
                model = pickle.load(model_file)

            # Load model_io_transformers from model directory
            transformers_module_name = 'model_io_transformers'
            transformers_source_path = os.path.join(model_base_path, '%s.py' % transformers_module_name)
            spec = importlib.util.spec_from_file_location(transformers_module_name, transformers_source_path)
            transformers_module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(transformers_module)

            self.registry[model_key] = (model_file_absolute_path, model_key, model, transformers_module)

        return (model_file_absolute_path, model_key, model, transformers_module)


class ModelDeployPython3Handler(tornado.web.RequestHandler):
    @REQUEST_TIME.time()
    def post(self, model_type, model_namespace, model_name, model_version):
        fileinfo = self.request.files['bundle'][0]
        model_file_source_bundle_path = fileinfo['filename']
        (_, filename) = os.path.split(model_file_source_bundle_path)
        bundle_path = os.path.join(self.settings['model_store_path'], model_type)
        bundle_path = os.path.join(bundle_path, model_namespace)
        bundle_path = os.path.join(bundle_path, model_name)
        bundle_path = os.path.join(bundle_path, model_version)
        bundle_path_filename = os.path.join(bundle_path, filename)
        try:
            os.makedirs(bundle_path, exist_ok=True)
            with open(bundle_path_filename, 'wb+') as fh:
                fh.write(fileinfo['body'])
            logger.info("'%s' uploaded '%s', saved as '%s'" %
                        ( str(self.request.remote_ip),
                          str(filename),
                          bundle_path_filename) )
            logger.info("Uploading and extracting bundle '%s' into '%s'..." % (filename, bundle_path))
            with tarfile.open(bundle_path_filename, "r:gz") as tar:
                tar.extractall(path=bundle_path)
            logger.info('...Done!')
            logger.info('Installing bundle and updating environment...\n')
            completed_process = subprocess.run('cd %s && ./install.sh' % bundle_path,
                                               timeout=600,
                                               shell=True,
                                               stdout=subprocess.PIPE)
            logger.info('...Done!')
            self.write('Model successfully deployed!')
        except IOError as e:
            logger.error('Failed to write file due to IOError %s' % str(e))
            self.write('Failed to write file due to IOError %s' % str(e))
            raise e


    def write_error(self, status_code, **kwargs):
        self.write('Error %s' % status_code)
        if "exc_info" in kwargs:
            self.write(", Exception: %s" % kwargs["exc_info"][0].__name__)


def main():
    # Start up a web server to expose request made and time spent metrics to Prometheus
    # TODO:  Potentially expose metrics to Prometheus using the Tornado HTTP server as long as it's not blocking
    #        See the MetricsHandler class which provides a BaseHTTPRequestHandler
    #        https://github.com/prometheus/client_python/blob/ce5542bd8be2944a1898e9ac3d6e112662153ea4/prometheus_client/exposition.py#L79
    logger.info("Starting Prometheus Http Server on port '%s'" % options.PIO_MODEL_SERVER_PROMETHEUS_PORT)
    start_http_server(options.PIO_MODEL_SERVER_PROMETHEUS_PORT)
    logger.info("Starting Model Predict and Deploy Http Server on port '%s'" % options.PIO_MODEL_SERVER_PORT)
    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(options.PIO_MODEL_SERVER_PORT)
    tornado.ioloop.IOLoop.current().start()


if __name__ == '__main__':
    main()
