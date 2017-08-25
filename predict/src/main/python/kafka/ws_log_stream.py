import tornado.httpserver
import tornado.websocket
import tornado.ioloop
from tornado.ioloop import PeriodicCallback
import tornado.web
from random import randint #Random generator
from urllib.parse import urlparse
import os
from kafka import KafkaConsumer

#Config
port = 5959 #Websocket Port
timeInterval= 1000 #Milliseconds

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
        # To consume latest messages and auto-commit offsets
        self.consumer = KafkaConsumer('prediction-inputs',
                         bootstrap_servers=['localhost:9092'])

        self.callback = PeriodicCallback(self.send_values, timeInterval)
        self.callback.start()

    def send_values(self):
        #Generates random values to send via websocket
        #self.write_message(str(randint(1,10)) + ';' + str(randint(1,10)) + ';' + str(randint(1,10)) + ';' + str(randint(1,10)))
        for record in self.consumer:
            # message value and key are raw bytes -- decode if necessary!
            # e.g., for unicode: `message.value.decode('utf-8')`
            #print ("%s:%d:%d: key=%s value=%s" % (message.topic, message.partition,
            #                                      message.offset, message.key,
            #                                      message.value))
            value = record.value.decode('utf-8')
            self.write_message(value)

    def on_message(self, message):
        pass

    def on_close(self):
        self.callback.stop()

settings = {
    "static_path": os.path.join(os.path.dirname(__file__), "static"),
}

application = tornado.web.Application([
    (r'/ws', WSHandler),
    (r'/(.*)', tornado.web.StaticFileHandler,
       dict(path=settings['static_path'])),

])

if __name__ == "__main__":
    http_server = tornado.httpserver.HTTPServer(application)
    http_server.listen(port)
    tornado.ioloop.IOLoop.instance().start()
