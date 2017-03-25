import tornado.ioloop
import tornado.web
import pickle

class MainHandler(tornado.web.RequestHandler):
  def get(self):
    self.write("Hello, world")
    self.write("Prediction: ", decision_tree_model.predict([[1,1,3,4]]))

def make_app():
  return tornado.web.Application([
      (r"/", MainHandler),
  ])

if __name__ == "__main__":
  decision_tree_pkl_filename = '1/python_balancescale.pkl'
  decision_tree_model_pkl = open(decision_tree_pkl_filename, 'rb')
  decision_tree_model = pickle.load(decision_tree_model_pkl)
  decision_tree_model_pkl.close()

  app = make_app()
  app.listen(9876)
  tornado.ioloop.IOLoop.current().start()
