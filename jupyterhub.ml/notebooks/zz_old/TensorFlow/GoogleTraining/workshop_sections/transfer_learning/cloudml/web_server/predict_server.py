# Copyright 2016 Google Inc. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""This is a little example web server that shows how to access the Cloud ML
API for prediction, using the google api client libraries. It lets the user
upload an image, then encodes that image and sends off the prediction request,
then displays the results.
It uses the default version of the specified model.  Some of the web app's
UI is hardwired for the "hugs" image set, but would be easy to modify for other
models.
See the README.md for how to start the server.
"""

import argparse
import base64
from cStringIO import StringIO
import os
import sys

from flask import Flask, redirect, render_template, request, url_for
from googleapiclient import discovery
from oauth2client.client import GoogleCredentials
from PIL import Image
from werkzeug import secure_filename


desired_width = 299
desired_height = 299

UPLOAD_FOLDER = 'static'
ALLOWED_EXTENSIONS = set(['jpg', 'jpeg', 'JPG'])

args = None
labels = None


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


@app.route('/', methods=['GET', 'POST'])
def upload_file():
    print "upload file"
    if request.method == 'POST':

        file = request.files['file']
        print file
        if file and allowed_file(file.filename):
            label, score = '', ''

            filename = secure_filename(file.filename)

            file.save(os.path.join(app.config['UPLOAD_FOLDER'],
                      filename))
            fname = "%s/%s" % (UPLOAD_FOLDER, filename)
            ml_client = create_client()
            result = get_prediction(ml_client, args.project,
                                    args.model_name, fname)
            predictions = result['predictions']
            print("predictions: %s" % predictions)
            prediction = predictions[0]
            print("prediction: %s" % prediction)
            label_idx = prediction['prediction']
            score = prediction['scores'][label_idx]
            label = labels[label_idx]
            print(label, score)
            return redirect(url_for('show_result',
                                    filename=filename,
                                    label=label,
                                    score=score))

    return render_template('index.html')


@app.route('/result')
def show_result():
    print "uploaded file"

    filename = request.args['filename']
    label = request.args['label']
    score = request.args['score']

    # This result handling logic is hardwired for the "hugs/not-hugs"
    # example, but would be easy to modify for some other set of
    # classification labels.
    if label == 'not-hugs':
        return render_template('jresults.html',
                               filename=filename,
                               label="Not so huggable.",
                               score=score,
                               border_color="#B20000")

    elif label == 'hugs':
        return render_template('jresults.html',
                               filename=filename,
                               label="Huggable.",
                               score=score,
                               border_color="#00FF48")
    else:
        return render_template('error.html',
                               message="Something went wrong.")


def create_client():

  credentials = GoogleCredentials.get_application_default()
  ml_service = discovery.build(
      'ml', 'v1beta1', credentials=credentials)
  return ml_service


def get_prediction(ml_service, project, model_name, input_image):
  request_dict = make_request_json(input_image)
  body = {'instances': [request_dict]}

  # This request will use the default model version.
  parent = 'projects/{}/models/{}'.format(project, model_name)
  request = ml_service.projects().predict(name=parent, body=body)
  result = request.execute()
  return result


def make_request_json(input_image):
  """..."""

  image = Image.open(input_image)
  resized_handle = StringIO()
  is_too_big = ((image.size[0] * image.size[1]) >
                (desired_width * desired_height))
  if is_too_big:
    image = image.resize(
        (desired_width, desired_height), Image.BILINEAR)

  image.save(resized_handle, format='JPEG')
  encoded_contents = base64.b64encode(resized_handle.getvalue())

  image_json = {'key': input_image,
                'image_bytes': {'b64': encoded_contents}}
  return image_json


def parse_args():
  parser = argparse.ArgumentParser()
  parser.add_argument(
      '--model_name', type=str, required=True,
      help='The name of the model.')
  parser.add_argument(
      '--dict',
      type=str,
      required=True,
      help='Path to dictionary file.')
  parser.add_argument(
      '--project', type=str, required=True,
      help=('The project name to use.'))
  args, _ = parser.parse_known_args(sys.argv)
  return args


def read_dictionary(path):
  with open(path) as f:
    return f.read().splitlines()


if __name__ == "__main__":

    args = parse_args()
    labels = read_dictionary(args.dict)
    print("labels: %s" % labels)
    # Runs on port 5000 by default.
    # Change to app.run(host='0.0.0.0') for an externally visible
    # server.
    app.run(debug=True)
