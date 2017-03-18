# -*- coding: utf-8 -*-

__version__ = "0.2"

import sys
import subprocess
import requests
from optparse import OptionParser
from sklearn2pmml import sklearn2pmml

# Tensorflow Inception
#  direct:  http://prediction-tensorflow-aws.demo.pipeline.io/evaluate-tensorflow-java-image/my_namespace/tensorflow_inception/00000001
#  api:  http://api.demo.pipeline.io/prediction-tensorflow/evaluate-tensorflow-java-image/my_namespace/tensorflow_inception/00000001

# SparkML
#  direct:  http://prediction-pmml-aws.demo.pipeline.io/evaluate-pmml/my_namespace/pmml_airbnb/1
#  api:  http://api.demo.pipeline.io/prediction-pmml/evaluate-pmml/my_namespace/pmml_airbnb/1

def main():
  print("PipelineIO CLI version %s." % __version__)
  print("Argument strings: %s" % sys.argv[1:])
  print("")
  print("Usage:")
  print("  pipelineio [options] <command> <namespace> <args>") 
  print("")
  parser = OptionParser()
  parser.add_option("-t", "--target", dest="target", help="target host:port")
  parser.add_option("-m", "--model", dest="model", help="model")
  parser.add_option("-v", "--version", dest="version", help="version")
  parser.add_option("-f", "--filename", dest="filename", help="filename")
  parser.add_option("-s", "--string", dest="string", help="string")

  (options, args) = parser.parse_args()

  command = args[0]
  print("Command: %s" % command)

  namespace = args[1]
  print("Namespace: %s" % namespace)
  
  if (command == "predict"):
    if (options.filename):
      # file-based
      predict_with_file(target=options.target, namespace=namespace, model=options.model, version=options.version, filename=options.filename)
      # string-based
    elif (options.string):
      predict_with_string(target=options.target, namespace=namespace, model=options.model, version=options.version, string=options.string)
    else:
      print("Invalid command.")
  elif (command == "update"):
    if (options.filename):
      update_with_file(target=options.target, model=options.model, version=options.version, filename=options.filename)
    elif (options.string):
      update_with_string(target=options.target, model=options.model, version=options.version, string=options.string)
  else:
    print("Invalid command.")
 
def predict_with_string(_sentinel=None, target=None, namespace=None, model=None, version=None, string=None):
  print("Evaluate string '%s'" % string)
  print("")

  url = "http://%s/evaluate-pmml/%s/%s/%s" % (target, namespace, model, version)
  print(url)
  print("")

  headers = {'Content-type': 'application/json', 'Accept': 'application/json'}
  response = requests.post(url, data=string, headers=headers, timeout=30)
  print(response.text)

def predict_with_file(_sentinel=None, target=None, namespace=None, model=None, version=None, filename=None):
  print("Evaluate file '%s'" % filename)
  print("")

  files = [('image', (filename, open(filename, 'rb')))]

  url = "http://%s/evaluate-tensorflow-java-image/%s/%s/%s" % (target, namespace, model, version)
  print(url)
  print("")

  response = requests.post(url, files=files, timeout=60)
  print(response.text)

def update_with_file(_sentinel=None, target=None, namespace=None, model=None, version=None, filename=None):
  print("Update model '%s'" % model)
  print("")

  files = [('model', (filename, open(filename, 'rb')))]

  url = "http://%s/update-tensorflow-model/%s/%s/%s" % (target, namespace, model, version)
  print(url)
  print("")

  response = requests.post(url, files=files, timeout=120)
  print(response.text)

main()
