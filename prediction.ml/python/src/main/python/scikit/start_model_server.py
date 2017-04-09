#!/usr/bin/env python

import tornado.ioloop
import tornado.web
import tornado.httpserver
import tornado.httputil
import tornado.gen
import json
import pickle
import numpy as np
from hystrix import Command
import asyncio

from transformers import input_transformer, output_transformer

class PredictCommand(Command):
    inputs = [[]]
    def run(self):
        return self.do_predict() 

    # TODO:  roll this into run() method
    def do_predict(self):
        return model.predict(self.inputs)

    def fallback(self):
        return 'fallback!'

class MainHandler(tornado.web.RequestHandler):
    @tornado.gen.coroutine
    def post(self):
        self.set_header("Content-Type", "application/json")
        command = self.build_command()
        output = yield self.build_future(command)   
        self.write(output_transformer(output))

    def build_command(self):
        command = PredictCommand()
        command.name = 'Predict'
        command.group_name = 'PredictGroup'
        # TODO:  convert json to pandas array
        command.inputs = input_transformer(self.json_args)
        return command

    def build_future(self, command):
        future = command.observe()
        future.add_done_callback(future.result)
        return future

    def prepare(self):
        if self.request.headers["Content-Type"].startswith("application/json"):
            #self.json_args = json.loads(self.request.body)
            self.json_args = self.request.body
        else:
            self.json_args = None

def load_model(model_pkl_filename):
    with open(model_pkl_filename, 'rb') as model_pkl:
        model = pickle.load(model_pkl)          
    return model

if __name__ == '__main__':
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('port')
    parser.add_argument('model_pkl_filename')
    args = parser.parse_args()

    model = load_model(args.model_pkl_filename)

    app = tornado.web.Application([
        (r"/", MainHandler),
    ])
    app.listen(args.port)


    print("")
    print("Started Tornado-based Http Server on Port %s" % args.port)
    print("")
    print("Loaded Model from Filename `%s`" % args.model_pkl_filename)
    print("")

    tornado.ioloop.IOLoop.current().start()
