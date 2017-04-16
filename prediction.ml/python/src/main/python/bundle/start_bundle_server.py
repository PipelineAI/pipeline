#!/usr/bin/env python3

import tornado.web
import logging
import os
import uuid
import tarfile
import subprocess

class UploadHandler(tornado.web.RequestHandler):
    def initialize(self, bundle_parent_path):
        self.bundle_parent_path = bundle_parent_path

    def post(self, model_namespace, model_name, model_version):
        fileinfo = self.request.files['bundle'][0]
        absolutepath = fileinfo['filename']
        (_, filename) = os.path.split(absolutepath)
        bundle_path = os.path.join(self.bundle_parent_path, model_namespace)
        bundle_path = os.path.join(bundle_path, model_name)
        bundle_path = os.path.join(bundle_path, model_version)
        bundle_path_filename = os.path.join(bundle_path, filename)
        try:
            os.makedirs(bundle_path, exist_ok=False)
            with open(bundle_path_filename, 'wb+') as fh:
                fh.write(fileinfo['body'])
            print("%s uploaded %s, saved as %s" %
                        ( str(self.request.remote_ip),
                          str(filename),
                          bundle_path_filename) )
            with tarfile.open(bundle_path_filename, "r:gz") as tar:
                tar.extractall(path=bundle_path)
            self.write('Successfully uploaded and extracted bundle %s into %s' % (filename, bundle_path))
            completed_process = subprocess.run('cd %s && ./install.sh' % bundle_path), 
                                               timeout=300, 
                                               shell=True,
                                               stdout=subprocess.PIPE)
            self.write('Installed and updated environment.')
        except IOError as e:
            print('Failed to write file due to IOError %s' % str(e))
            self.write('Failed to write file due to IOError %s' % str(e))
            raise e

    def write_error(self, status_code, **kwargs):
        self.write('Error %s' % status_code)
        if "exc_info" in kwargs:
            self.write(", Exception: %s" % kwargs["exc_info"][0].__name__)

if __name__ == '__main__':
    port = os.environ['PIO_BUNDLE_SERVER_PORT'] 
    bundle_parent_path = os.environ['STORE_HOME']

    app = tornado.web.Application([
      # url: /$PIO_NAMESPACE/$PIO_MODELNAME/$PIO_MODEL_VERSION/
      (r"/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)/([a-zA-Z\-0-9\.:,_]+)", UploadHandler,
         dict(bundle_parent_path=bundle_parent_path))
    ])

    app.listen(port)

    print("")
    print("Started Tornado-based Bundle Server on Port %s" % port)
    print("")
    print("Watching bundles within path %s" % bundle_parent_path)

    tornado.ioloop.IOLoop.current().start()
