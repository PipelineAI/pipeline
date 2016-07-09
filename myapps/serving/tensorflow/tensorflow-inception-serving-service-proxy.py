import subprocess

from flask import Flask, json, request, after_this_request
import requests
import numpy as np

app = Flask(__name__)

@app.route('/classify/<image_url>')
def classify_image(image_url):
#    wgetcmd = 'wget %s' % image_url
#    p = subprocess.Popen(wgetcmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
#    for line in p.stdout.readlines():
#        print line,
#    retval = p.wait()

    # TODO  get filename from url

    classifycmd = '$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=$DATASETS_HOME/inception/%s' % image_url
    p = subprocess.Popen(classifycmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    # Skip first 2 lines
    p.stdout.readline()
    p.stdout.readline()

    results = []
    for line in p.stdout.readlines():
        results.append(line)
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
