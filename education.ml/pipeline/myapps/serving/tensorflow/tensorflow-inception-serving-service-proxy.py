import subprocess

from flask import Flask, json, request, after_this_request
import requests
import numpy as np
import urllib
import time

app = Flask(__name__)

@app.route('/classify')
def classify_image():
    imageDownloader = urllib.URLopener()
   
    image_url = request.args.get('image_url')
 
    image_name = '/tmp/%d' % int(round(time.time() * 1000))

    print '%s -> %s' % (image_url, image_name)

    imageDownloader.retrieve(image_url, image_name)
    
    classifycmd = '$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=%s' % image_name

    p = subprocess.Popen(classifycmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    # Skip first 3 lines
    p.stdout.readline()
    p.stdout.readline()
    p.stdout.readline()

    results = []
    for line in p.stdout.readlines():
        results.append(line.rstrip())
    retval = p.wait()

    @after_this_request
    def add_header(response):
        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response
    return json.dumps({'retval':retval,'results':results})

    
@app.route('/health')
def health():
    # TODO:  perfom a classification and return 'DOWN' or 'UP' based on the result
    #        ie. if result.contains('Failed"), return 'DOWN'
    #return json.dumps({'status': 'UP'})
    @after_this_request
    def add_header(response):
        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response
    return json.dumps({'status': 'UP'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5070')
