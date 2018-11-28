#!/usr/bin/env python3

import os
import logging
import tornado.httpserver
import tornado.websocket
import tornado.ioloop
import tornado.web
from tornado.ioloop import PeriodicCallback
from tornado.options import define, options
from random import randint #Random generator
from urllib.parse import urlparse
from confluent_kafka import Consumer, KafkaError

LOGGER = logging.getLogger(__name__)
LOGGER.setLevel(logging.ERROR)

CH = logging.StreamHandler()
CH.setLevel(logging.ERROR)
LOGGER.addHandler(CH)

TORNADO_ACCESS_LOGGER = logging.getLogger('tornado.access')
TORNADO_ACCESS_LOGGER.setLevel(logging.ERROR)

TORNADO_APPLICATION_LOGGER = logging.getLogger('tornado.application')
TORNADO_APPLICATION_LOGGER.setLevel(logging.ERROR)

TORNADO_GENERAL_LOGGER = logging.getLogger('tornado.general')
TORNADO_GENERAL_LOGGER.setLevel(logging.ERROR)

define('PIPELINE_WEBSOCKET_KAFKA_SERVER_PORT', default='', help='tornado http websocket kafka server listen port', type=int)
define('PIPELINE_WEBSOCKET_KAFKA_SERVER_UPDATE_INTERVAL', default='', help='interval to update from kafka topic', type=int)

class HealthzHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.finish()
        except Exception as e:
            logging.exception('HealthzHandler.get: Exception {0}'.format(str(e)))


class WSKafkaHandler(tornado.websocket.WebSocketHandler):
    #check_origin fixes an error 403 with Tornado
    #http://stackoverflow.com/questions/24851207/tornado-403-get-warning-when-opening-websocket
    def check_origin(self, origin):
        CORS_ORIGINS = ['127.0.0.1', 'localhost', 'pipeline.ai', 'community.pipeline.ai']
        parsed_origin = urlparse(origin)
        # parsed_origin.netloc.lower() gives localhost:3333
        return (parsed_origin.hostname in CORS_ORIGINS) or parsed_origin.hostname.endswith('.pipeline.ai')

    def open(self, topic_name):
    #Send message periodic via socket upon a time interval
        # To consume latest messages and auto-commit offsets
        #self.consumer = KafkaConsumer(topic_name, bootstrap_servers=['localhost:9092'])
        self.consumer = Consumer({'bootstrap.servers': 'localhost:9092'})
        self.consumer.subscribe(['topic_name'])

        self.callback = PeriodicCallback(self.send_values, options.PIPELINE_WEBSOCKET_KAFKA_SERVER_UPDATE_INTERVAL)
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
            try:
                value = record.value.decode('utf-8')
                self.write_message(value)
            except Exception as e:
                logging.exception('WSHandler.send_values: Exception {0}'.format(str(e)))
                self.write_message('Error {0}'.format(str(e)))

    def on_message(self, message):
        pass

    def on_close(self):
        self.callback.stop()


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/stream/kafka/(.*)', tornado.web.StaticFileHandler,
                dict(default_filename='index.html',
                     path=os.path.join(os.path.dirname(__file__), 'static'))),
            (r'/stream/ws/kafka/([a-zA-Z\-0-9\.:,_]+)', WSKafkaHandler),
            (r'/healthz', HealthzHandler),
        ]
        tornado.web.Application.__init__(self, handlers,)

    def fallback(self):
        LOGGER.warn('Model Server Application fallback: {0}'.format(self))
        return 'Fallback!'


def main():
    try:
        tornado.options.parse_command_line()
        if not options.PIPELINE_WEBSOCKET_KAFKA_SERVER_PORT or not options.PIPELINE_WEBSOCKET_KAFKA_SERVER_UPDATE_INTERVAL:
            LOGGER.error('--PIPELINE_WEBSOCKET_KAFKA_SERVER_PORT and --PIPELINE_WEBSOCKET_KAFKA_SERVER_UPDATE_INTERVAL must be set')
            return

        LOGGER.info('WebSocket Kafka Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_WEBSOCKET_KAFKA_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIPELINE_WEBSOCKET_KAFKA_SERVER_PORT)
        LOGGER.info('WebSocket Kafka Server main: complete start tornado-based http server port {0}'.format(options.PIPELINE_WEBSOCKET_KAFKA_SERVER_UPDATE_INTERVAL))

        tornado.ioloop.IOLoop.current().start()
        print('...Python-based WebSocket Kafka Server Started!')
    except Exception as e:
        LOGGER.info('ws_kafka_topic_stream.main: Exception {0}'.format(str(e)))
        logging.exception('ws_kafka_topic_stream.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
