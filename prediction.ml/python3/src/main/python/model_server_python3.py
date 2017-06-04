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

from pio_model import PioRequestTransformer
from pio_model import PioResponseTransformer
from pio_model import PioModelInitializer
from pio_model import PioModel

define("PIO_MODEL_STORE_HOME", default="model_store", help="path to model_store", type=str)
define("PIO_MODEL_TYPE", default="", help="prediction model type", type=str)
define("PIO_MODEL_NAMESPACE", default="", help="prediction model namespace", type=str)
define("PIO_MODEL_NAME", default="", help="prediction model name", type=str)
define("PIO_MODEL_VERSION", default="", help="prediction model version", type=str)
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
            #(r"/", IndexHandler),
            # TODO:  Disable DEPLOY if we're not explicitly in PIO_MODEL_ENVIRONMENT=dev mode (or equivalent)
            # url: /api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r"/api/v1/model/deploy/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelDeployPython3Handler),
            # url: /api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
            (r"/api/v1/model/predict/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", ModelPredictPython3Handler),
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
        model = self.get_model_assets(model_type,
                                      model_namespace,
                                      model_name,
                                      model_version)
        print(self.request.body)
        response = model.predict(self.request.body)
        self.write(response)
        self.finish()


    @REQUEST_TIME.time()
    def get_model_assets(self, model_type, model_namespace, model_name, model_version):
        model_key = '%s_%s_%s_%s' % (model_type, model_namespace, model_name, model_version)
        if model_key not in self.registry:
            model_store_path = self.settings['model_store_path']
            model_store_path = os.path.expandvars(model_store_path)
            model_store_path = os.path.expanduser(model_store_path)
            model_store_path = os.path.abspath(model_store_path)

            bundle_path = os.path.join(model_store_path, model_type)
            bundle_path = os.path.join(bundle_path, model_namespace)
            bundle_path = os.path.join(bundle_path, model_name)
            bundle_path = os.path.join(bundle_path, model_version)

            model_file_absolute_path = os.path.join(bundle_path, "pio_model.pkl")

            # Load pickled model from model directory
            with open(model_file_absolute_path, 'rb') as model_file:
                model = pickle.load(model_file)

            self.registry[model_key] = model

        return self.registry[model_key]


class ModelDeployPython3Handler(tornado.web.RequestHandler):
    @REQUEST_TIME.time()
    def post(self, model_type, model_namespace, model_name, model_version):
        fileinfo = self.request.files['file'][0]
        model_file_source_bundle_path = fileinfo['filename']
        (_, filename) = os.path.split(model_file_source_bundle_path)

        model_base_path = self.settings['model_store_path']
        model_base_path = os.path.expandvars(model_base_path)
        model_base_path = os.path.expanduser(model_base_path)
        model_base_path = os.path.abspath(model_base_path)

        bundle_path = os.path.join(model_base_path, model_type)
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
            logger.info("Uploaded and extracting bundle '%s' into '%s'..." % (filename, bundle_path))
            with tarfile.open(bundle_path_filename, "r:gz") as tar:
                tar.extractall(path=bundle_path)
            logger.info('...Done!')
            logger.info('')
            logger.info('Installing bundle and updating environment...\n')
            completed_process = subprocess.run('cd %s && [ -s ./requirements_conda.txt ] && conda install --yes --file ./requirements_conda.txt' % bundle_path,
                                               timeout=600,
                                               shell=True,
                                               stdout=subprocess.PIPE)
            completed_process = subprocess.run('cd %s && [ -s ./requirements.txt ] && pip install -r ./requirements.txt' % bundle_path,
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
    start_http_server(int(options.PIO_MODEL_SERVER_PROMETHEUS_PORT))
    logger.info("Starting Model Predict and Deploy Http Server on port '%s'" % options.PIO_MODEL_SERVER_PORT)
    http_server = tornado.httpserver.HTTPServer(Application())
    http_server.listen(int(options.PIO_MODEL_SERVER_PORT))
    tornado.ioloop.IOLoop.current().start()


if __name__ == '__main__':
    main()
