#!/usr/bin/env python3

from io import StringIO
import logging
import os
import tornado.ioloop
import tornado.options
import tornado.web
import tornado.httpserver
import importlib.util
import tarfile
import subprocess
from tornado.options import define, options
import json


# Test
#curl -i -X POST -v -H "Transfer-Encoding: chunked" \
#   -F "file=@pipeline.tar.gz" \
#   http://[host]:[port]/api/v1/model/drop/<model_type>/<model_name>

define('PIPELINE_DROP_PATH', default='', help='path to drop', type=str)
define('PIPELINE_DROP_SERVER_PORT', default='', help='tornado http drop server listen port', type=int)

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


class HealthzHandler(tornado.web.RequestHandler):

    @tornado.web.asynchronous
    def get(self):
        try:
            self.set_status(200, None)
            self.add_header('Content-Type', 'text/plain')
            self.finish()
        except Exception as e:
            logging.exception('HealthzHandler.get: Exception {0}'.format(str(e)))


class ModelDropPython3Handler(tornado.web.RequestHandler):
    def set_default_headers(self):
        self.set_header('Access-Control-Allow-Origin', '*')
        self.set_header('Access-Control-Allow-Credentials', 'true')
        self.set_header('Access-Control-Allow-Headers', 'Cache-Control, Origin, X-Requested-With, Content-Type, Accept, Authorization')
        self.set_header('Access-Control-Allow-Methods', 'POST, GET, PUT, DELETE, OPTIONS')

    def options(self, *args, **kwargs):
        self.set_status(204)
        self.finish()

    def post(self, model_type, model_name, model_tag):
        model_key_list = [model_type, model_name, model_tag]
        model_key = '/'.join(model_key_list)

        fileinfo = self.request.files['file'][0]
        model_file_source_drop_path = fileinfo['filename']
        (_, filename) = os.path.split(model_file_source_drop_path)

        drop_path = options.PIPELINE_DROP_PATH
            #self.settings['drop_path']
        drop_path = os.path.expandvars(drop_path)
        drop_path = os.path.expanduser(drop_path)
        drop_path = os.path.abspath(drop_path)
        drop_path = os.path.join(drop_path, 'model_drops')
        drop_path = os.path.join(drop_path, *model_key_list)
        from datetime import datetime
        #model_tag = datetime.now().strftime("%s")
        drop_path = os.path.join(drop_path, model_tag)
        drop_path_filename = os.path.join(drop_path, filename)

        os.makedirs(drop_path, exist_ok=True)
        with open(drop_path_filename, 'wb+') as fh:
            try:
                fh.write(fileinfo['body'])
                LOGGER.info('{0} uploaded {1}, saved as {2}'.format(str(self.request.remote_ip), str(filename),
                                                                drop_path_filename))
                LOGGER.info('Extracting drop {0} into {1}: begin'.format(filename, drop_path_filename))
                print(drop_path)
                print(drop_path_filename)
                with tarfile.open(drop_path_filename, 'r:gz') as tar:
                    tar.extractall(path=drop_path)
                LOGGER.info('Extracting drop {0} into {1}: complete'.format(filename, drop_path))

                LOGGER.info('Processing drop: begin')
                # TODO:  Try tornado.subprocess to stream the results as they're happening!
                cmd = 'pipeline model-build --model-type={0} --model-name={1} --model-tag={2} --model-path=./model_drops/{0}/{1}/{2} --build-path=/root/drop'.format(model_type, model_name, model_tag)
                LOGGER.info("Running command '%s'" % cmd)
                print(cmd)
                completed_process = subprocess.run(cmd,
                                                   timeout=1200,
                                                   shell=True,
                                                   stdout=subprocess.PIPE)

                cmd = 'pipeline model-push --model-type={0} --model-name={1} --model-tag={2}'.format(model_type, model_name, model_tag)
                print(cmd)
                completed_process = subprocess.run(cmd,
                                                   timeout=1200,
                                                   shell=True,
                                                   stdout=subprocess.PIPE)

                cmd = 'pipeline model-yaml --model-type={0} --model-name={1} --model-tag={2} --template-path=./drop/templates'.format(model_type, model_name, model_tag)
                print(cmd)
                completed_process = subprocess.run(cmd,
                                                   timeout=1200,
                                                   shell=True,
                                                   stdout=subprocess.PIPE)

                cmd = 'pipeline kube-create --yaml-path=./{0}-{1}-cpu-{2}-deploy.yaml'.format(model_type, model_name, model_tag)
                print(cmd)
                completed_process = subprocess.run(cmd,
                                                   timeout=1200,
                                                   shell=True,
                                                   stdout=subprocess.PIPE)

                cmd = 'pipeline kube-create --yaml-path=./{0}-{1}-cpu-{2}-svc.yaml'.format(model_type, model_name, model_tag)
                print(cmd)
                completed_process = subprocess.run(cmd,
                                                   timeout=1200,
                                                   shell=True,
                                                   stdout=subprocess.PIPE)
                LOGGER.info('Processing drop: complete')

                LOGGER.info('"{0}" successfully deployed!'.format(model_key))
                self.write('"{0}" successfully deployed!'.format(model_key))
            except Exception as e:
                message = 'DropPython3Handler.post: Exception - {0} Error {1}'.format(model_key, str(e))
                LOGGER.info(message)
                logging.exception(message)
            finally:
                LOGGER.info('Removing drop {0} from {1}: complete'.format(filename, drop_path))
                os.remove(drop_path_filename)
                LOGGER.info('Removing drop {0} into {1}: complete'.format(filename, drop_path))


class Application(tornado.web.Application):
    def __init__(self):
        handlers = [
            (r'/healthz', HealthzHandler),

            # url: /api/v1/model/drop/$PIPELINE_MODEL_TYPE/$PIPELINE_MODEL_NAME/$PIPELINE_MODEL_TAG
            (r'/api/v1/model/drop/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)',
             ModelDropPython3Handler),
        ]
        tornado.web.Application.__init__(self, handlers,) 

    def fallback(self):
        LOGGER.warn('Model Server Application fallback: {0}'.format(self))
        return 'fallback!'


def main():
    try:
        tornado.options.parse_command_line()
        if not options.PIPELINE_DROP_SERVER_PORT or not options.PIPELINE_DROP_PATH:
            LOGGER.error('--PIPELINE_DROP_SERVER_PORT and --PIPELINE_DROP_PATH must be set')
            return

        LOGGER.info('Drop Server main: begin start tornado-based http server port {0}'.format(options.PIPELINE_DROP_SERVER_PORT))
        http_server = tornado.httpserver.HTTPServer(Application())
        http_server.listen(options.PIPELINE_DROP_SERVER_PORT)
        LOGGER.info('Drop Server main: complete start tornado-based http server port {0}'.format(options.PIPELINE_DROP_SERVER_PORT))

        tornado.ioloop.IOLoop.current().start()
        print('...Python-based Drop Server Started!')
    except Exception as e:
        LOGGER.info('drop_server_python.main: Exception {0}'.format(str(e)))
        logging.exception('drop_server_python.main: Exception {0}'.format(str(e)))


if __name__ == '__main__':
    main()
