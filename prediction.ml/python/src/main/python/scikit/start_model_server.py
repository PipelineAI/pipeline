#!/usr/bin/env python3

import os
import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import pickle
from hystrix import Command
import fnmatch

from io_transformers import transform_inputs, transform_outputs

class PredictCommand(Command):

    def __init__(self, 
                 inputs, 
                 model, 
#                 io_transformers 
                 command_name, 
                 group_name, 
                 *args, 
                 **kwargs):

        super().__init__(*args, **kwargs)
        self.inputs = inputs
        self.model = model
#        self.io_transformers = io_transformers
        self.command_name = command_name
        self.group_name = group_name

    def run(self):
        print(self.inputs)
        transformed_inputs = self.transform_inputs(self.inputs)
        print(self.transformed_inputs)
        outputs = self.model.predict(transformed_inputs)
        print(outputs)
        transformed_outputs = self.transform_outputs(outputs)
        print(transformed_outputs)
        return transformed_outputs

    def fallback(self):
        print(self)
        return 'fallback!'

class MainHandler(tornado.web.RequestHandler):

    @tornado.gen.coroutine
    def post(self, model_namespace, model_name, model_version):
        command = self.build_command(model_namespace, model_name, model_version)
        output = yield self.build_future(command)
        self.write(output)

    def build_command(self, model_namespace, model_name, model_version):
        model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
        if model_key in model_registry:
            model = model_registry[model_key]
        else:
            _, model = load_model(model_namespace, model_name, model_version)
            model_registry[model_key] = model
        return PredictCommand(inputs=self.request.body,
                              model=model,
                              command_name='Predict_%s' % model_key,
                              group_name='PredictGroup')

    def build_future(self, command):
        future = command.observe()
        future.add_done_callback(future.result)
        return future


def load_model(model_namespace, model_name, model_version):
    model_absolute_path = os.path.join(store_home, model_namespace)
    model_absolute_path = os.path.join(model_absolute_path, model_name)
    model_absolute_path = os.path.join(model_absolute_path, model_version)

    model_filename = fnmatch.filter(os.listdir(model_absolute_path), "*.pkl")[0]

    model_absolute_path = os.path.join(model_absolute_path, model_filename)

    with open(model_absolute_path, 'rb') as model_file:
        model = pickle.load(model_file)
    return (model_absolute_path, model)


if __name__ == '__main__':
    port = os.environ['PIO_MODEL_SERVER_PORT']

    store_home = os.environ['STORE_HOME']
    model_namespace = os.environ['PIO_MODEL_NAMESPACE']
    model_name = os.environ['PIO_MODEL_NAME']
    model_version = os.environ['PIO_MODEL_VERSION']

    model_registry = {}

    model_key = '%s_%s_%s' % (model_namespace, model_name, model_version)
    (model_absolute_path, model) = load_model(model_namespace, model_name, model_version)

    model_registry[model_key] = model

    app = tornado.web.Application([
      # url: /$PIO_NAMESPACE/$PIO_MODELNAME/$PIO_MODEL_VERSION/
      (r"/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", MainHandler)
    ])
    app.listen(port)

    print("")
    print("Started Tornado-based Http Server on Port '%s'" % port)
    print("")
    print("Loaded Model from `%s`" % model_absolute_path)
    print("")

    tornado.ioloop.IOLoop.current().start()
