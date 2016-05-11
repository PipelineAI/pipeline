import subprocess

from flask import Flask, json, request
import requests
import numpy as np

app = Flask(__name__)

@app.route('/classify-image/<image_url>')
def classify_image(image_url):
#    wgetcmd = 'wget %s' % image_url
#    p = subprocess.Popen(wgetcmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
#    for line in p.stdout.readlines():
#        print line,
#    retval = p.wait()

    # TODO  get filename from url

    classifycmd = '$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=$DATASETS_HOME/inception/cropped_panda.jpg'

    p = subprocess.Popen(classifycmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    #for line in p.stdout.readlines():
    #    print line,
    retval = p.wait()
    p.stdout.readline()
    return json.dumps(p.stdout.readline())

if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5070')
