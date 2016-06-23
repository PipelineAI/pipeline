from flask import Flask, json, request
import requests
import tensorflow as tf
from tensorflow_serving.session_bundle import exporter

app = Flask(__name__)

@app.route('/v1/tf/<tf-version>/model-update/<model_type>/<model_url>')
def model_update(model_type, model_url):
  # TODO
  # https://github.com/tensorflow/serving/blob/master/tensorflow_serving/example/mnist_export.py
  # https://github.com/tensorflow/serving/blob/master/tensorflow_serving/session_bundle/exporter.py
  # https://github.com/tensorflow/serving/blob/master/tensorflow_serving/session_bundle/README.md
  export_path = '/tmp/v1/tf/0.9/model-update/%s/%s' % model_type, model_url
  print 'Exporting trained model to', export_path
  #saver = tf.train.Saver(sharded=True)
  #model_exporter = exporter.Exporter(saver)
  #signature = exporter.classification_signature(input_tensor=x, scores_tensor=y)
  #model_exporter.init(sess.graph.as_graph_def(),
  #                    default_graph_signature=signature)
  #model_exporter.export(export_path, tf.constant(FLAGS.export_version), sess)
  #print 'Done exporting!'
  print `TODO`

if __name__ == '__main__':
    app.run(host='0.0.0.0', port='5040')
