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

class HelloWorldCommand(Command):
  def run(self):
    return self.do_post([[1,1,3,4]]) 

  def do_post(self, inputs):
    return decision_tree_model.predict(inputs).tolist()

  def fallback(self):
    return 'fallback!'

class MainHandler(tornado.web.RequestHandler):
  @tornado.gen.coroutine
  def post(self):
    self.set_header("Content-Type", "application/json")

    command = self.build_command()

    do_post_result = yield self.build_future(command)   

    self.write(json.dumps(do_post_result))

  def build_command(self):
    command = HelloWorldCommand()
    command.name = 'Hello'
    command.group_name = 'HelloGroup'
    return command

  def build_future(self, command):
    future = command.observe()
    future.add_done_callback(future.result)
    return future

#  def prepare(self):
#    if self.request.headers["Content-Type"].startswith("application/json"):
#        self.json_args = json.loads(self.request.body)
#    else:
#        self.json_args = None

if __name__ == "__main__":
  decision_tree_pkl_filename = '1/python_balancescale.pkl'
  decision_tree_model_pkl = open(decision_tree_pkl_filename, 'rb')
  decision_tree_model = pickle.load(decision_tree_model_pkl)
  decision_tree_model_pkl.close()

  app = tornado.web.Application([
      (r"/", MainHandler),
  ])
  app.listen(9876)

  tornado.ioloop.IOLoop.current().start()
