import tornado.httpserver
import tornado.websocket
import tornado.ioloop
from tornado.ioloop import PeriodicCallback
import tornado.web
from random import randint #Random generator
from urllib.parse import urlparse

#Config
port = 5959 #Websocket Port
timeInterval= 2000 #Milliseconds

class WSHandler(tornado.websocket.WebSocketHandler):
    #check_origin fixes an error 403 with Tornado
    #http://stackoverflow.com/questions/24851207/tornado-403-get-warning-when-opening-websocket
    def check_origin(self, origin):
        CORS_ORIGINS = ['localhost']
        parsed_origin = urlparse(origin)
        # parsed_origin.netloc.lower() gives localhost:3333
        return parsed_origin.hostname in CORS_ORIGINS

    def open(self):
    #Send message periodic via socket upon a time interval
        self.callback = PeriodicCallback(self.send_values, timeInterval)
        self.callback.start()

    def send_values(self):
    #Generates random values to send via websocket
        self.write_message(str(randint(1,10)) + ';' + str(randint(1,10)) + ';' + str(randint(1,10)) + ';' + str(randint(1,10)))

    def on_message(self, message):
        pass

    def on_close(self):
        self.callback.stop()

application = tornado.web.Application([
    (r'/', WSHandler),
])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(port)
    tornado.ioloop.IOLoop.instance().start()
