#!/usr/bin/env python3

import tornado.web
import logging
import os
import uuid

def original_naming_stategy(original_name):
    return original_name

def uuid_naming_strategy(original_name):
    "File naming strategy that ignores original name and returns an UUID"
    return str(uuid.uuid4())

class UploadHandler(tornado.web.RequestHandler):
    "Handle file uploads."

#    def initialize(self, upload_path, naming_strategy):
    def initialize(self):
        """Initialize with given upload path and naming strategy.
        :keyword upload_path: The upload path.
        :type upload_path: str
        :keyword naming_strategy: File naming strategy.
        :type naming_strategy: (str) -> str function
        """
#        self.upload_path = upload_path
#        if naming_strategy is None:
#        naming_strategy = original_naming_strategy
#        self.naming_strategy = naming_strategy

    def post(self):
        fileinfo = self.request.files['bundle'][0]
#        filename = self.naming_strategy(fileinfo['filename'])
        filename = fileinfo['filename']
        try:
#            with open(os.path.join(self.upload_path, filename), 'w') as fh:
            with open(os.path.join('.', filename), 'wb') as fh:
                fh.write(fileinfo['body'])
            logging.info("%s uploaded %s, saved as %s",
                         str(self.request.remote_ip),
                         str(fileinfo['filename']),
                         filename)
        except IOError as e:
            logging.error("Failed to write file due to IOError %s", str(e))

if __name__ == '__main__':
    from argparse import ArgumentParser
    parser = ArgumentParser()
#    parser.add_argument('port')
#    parser.add_argument('bundle_parent_path')
    args = parser.parse_args()

    app.listen(port)

    print("")
    print("Started Tornado-based Bundle Server on Port %s" % port)
    print("")
    print("Watching bundle parent path %s" % bundle_parent_path)

    tornado.ioloop.IOLoop.current().start()
