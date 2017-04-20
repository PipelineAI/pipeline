#!/usr/bin/env python3

import os
import sys
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import dill as pickle
from hystrix import Command
import fnmatch
import importlib.util

class PredictCommand(Command):

    def __init__(self,
                 inputs,
                 model,
                 transformers_module,
                 command_name,
                 group_name,
                 *args,
                 **kwargs):

        super().__init__(*args, **kwargs)
        self.inputs = inputs
        self.model = model
        self.transformers_module = transformers_module
        self.command_name = command_name
        self.group_name = group_name

    def run(self):
        transformed_inputs = self.transformers_module.transform_inputs(self.inputs)
        outputs = self.model.predict(transformed_inputs)
        transformed_outputs = self.transformers_module.transform_outputs(outputs)
        return transformed_outputs

    def fallback(self):
        print(self)
        return 'fallback!'

class MainHandler(tornado.web.RequestHandler):
    def initialize(self, bundle_parent_path):
        self.bundle_parent_path = bundle_parent_path
        self.registry = {}

    @tornado.gen.coroutine
    def post(self, model_namespace, model_name, model_version):
        (_, model_key, model, transformers_module) = self.get_model_assets(model_namespace,
                                                                           model_name,
                                                                           model_version)

#        command = self.build_command(model_key, model, transformers_module)
#
#        output = yield self.build_future(command)
#        self.write(output)
#
#    def build_command(self, model_key, model, transformers_module):
#        return PredictCommand(inputs=self.request.body,
#                              model=model,
#                              transformers_module=transformers_module,
#                              command_name='Predict_%s' % model_key,
#                              group_name='PredictGroup')
#
#    def build_future(self, command):
#        future = command.observe()
#        future.add_done_callback(future.result)
#        return future

        output = yield self.do_post(self.request.body, model, transformers_module)

    @tornado.gen.coroutine
    def do_post(self, inputs, model, transformers_module):
        transformed_inputs = transformers_module.transform_inputs(inputs)
        outputs = model.predict(transformed_inputs)
        transformed_outputs = transformers_module.transform_outputs(outputs)
        return transformed_outputs


    def get_model_assets(self, model_namespace, model_name, model_version):
        model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
        if model_key in self.registry:
            (model_absolute_path, model_key, model, transformers_module) = self.registry[model_key]
        else:
            model_base_path = os.path.join(self.bundle_parent_path, model_namespace)
            model_base_path = os.path.join(model_base_path, model_name)
            model_base_path = os.path.join(model_base_path, model_version)

            model_filename = fnmatch.filter(os.listdir(model_base_path), "*.pkl")[0]

            # Set absolute path to model directory
            model_absolute_path = os.path.join(model_base_path, model_filename)

            # Load pickled model from model directory
            with open(model_absolute_path, 'rb') as model_file:
                model = pickle.load(model_file)

            # Load model_io_transformers from model directory
            transformers_module_name = 'model_io_transformers'
            transformers_source_path = os.path.join(model_base_path, '%s.py' % transformers_module_name)
            spec = importlib.util.spec_from_file_location(transformers_module_name, transformers_source_path)
            transformers_module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(transformers_module)

            self.registry[model_key] = (model_absolute_path, model_key, model, transformers_module)

        return (model_absolute_path, model_key, model, transformers_module)


if __name__ == '__main__':
    port = os.environ['PIO_MODEL_SERVER_PORT']

    bundle_parent_path = os.environ['STORE_HOME']
    model_namespace = os.environ['PIO_MODEL_NAMESPACE']
    model_name = os.environ['PIO_MODEL_NAME']
    model_version = os.environ['PIO_MODEL_VERSION']

    app = tornado.web.Application([
      # url: /v1/$PIO_NAMESPACE/$PIO_MODELNAME/$PIO_MODEL_VERSION/
      (r"/v1/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", MainHandler,
          dict(bundle_parent_path=bundle_parent_path))
    ])
    app.listen(port)

    print("")
    print("Started Tornado-based Http Server on Port '%s'" % port)
    print("")
#    print("Loaded Model from '%s'" % model_absolute_path)
#    print("")
#    print("Transformers Loaded from '%s'" % transformers_module.__file__)
#    print("")

    tornado.ioloop.IOLoop.current().start()
