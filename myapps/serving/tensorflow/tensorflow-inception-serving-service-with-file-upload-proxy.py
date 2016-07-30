import subprocess

from flask import Flask, json, request, after_this_request
from flask_cors import CORS, cross_origin
import requests
import numpy as np
import urllib
import time
import os


app = Flask(__name__)
CORS(app)
app.config.from_object(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'

ALLOWED_EXTENSIONS = ['png', 'jpg']


@app.route('/upload', methods = ['POST'])
def upload():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            now = datetime.now()
            filename = os.path.join(app.config['UPLOAD_FOLDER'], "%s.%s" % (now.strftime("%Y-%m-%d-%H-%M-%S-%f"), file.filename.rsplit('.', 1)[1]))
            file.save(filename)
            print 'processing %s' % filename
            classifyupload = '$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=%s' % filename
            
            pu = subprocess.Popen(cats, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
              # Skip first 2 lines
    #pu.stdout.readline()
    #pu.stdout.readline()

    results = []
    #for line in pu.stdout.readlines():
     #   results.append(line.rstrip())
    #retval = pu.wait()

    @after_this_request
    def add_header(response):
        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response
    return json.dumps({'succesfully pretended to clasify':'filename'})
        #return json.dumps({'retval':retval,'results':results})

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.',1)[1] in ALLOWED_EXTENSIONS


        

@app.route('/classify')
def classify_image():
    imageDownloader = urllib.URLopener()
   
    image_url = request.args.get('image_url')
 
    image_name = '/tmp/%d' % int(round(time.time() * 1000))

    print '%s -> %s' % (image_url, image_name)

    imageDownloader.retrieve(image_url, image_name)
    
    classifycmd = '$TENSORFLOW_SERVING_HOME/bazel-bin/tensorflow_serving/example/inception_client --server=localhost:9091 --image=%s' % image_name

    stub = './fake.sh' #use this to test html before the service -- sub classifycmd below
    
    p = subprocess.Popen(stub, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

    # Skip first 2 lines
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
    return json.dumps({'succesfully pretended to clasify':image_url, 'namedIt':image_name})
    #return json.dumps({'retval':retval,'results':results})

    
@app.route('/health')
def health():
    # TODO:  perfom a classification and return 'DOWN' or 'UP' based on the result
    #        ie. if result.contains('Failed"), return 'DOWN'
    #return json.dumps({'status': 'UP'})
    @after_this_request
    def add_header(response):
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'Content-Type,Authorization'
        response.headers['Access-Control-Allow-Methods'] = 'GET,POST'
        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response
    return json.dumps({'status': 'UP'})




if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5070')
