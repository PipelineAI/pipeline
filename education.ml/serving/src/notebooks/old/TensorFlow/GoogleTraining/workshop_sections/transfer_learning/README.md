
This directory contains two examples of transfer learning using the "Inception V3" image classification model.

The [cloudml](cloudml) example shows how to use [Cloud Dataflow](https://cloud.google.com/dataflow/) ([Apache
Beam](https://beam.apache.org/)) to do image preprocessing, then train and serve your model on Cloud ML.  It supports
distributed training on Cloud ML.
It is based on the example [here](https://github.com/GoogleCloudPlatform/cloudml-samples/tree/master/flowers), with
some additional modifications to make it easy to use other image sets, and a prediction web server that demos how to
use the Cloud ML API for prediction once your trained model is serving.

The [TF_Estimator](TF_Estimator) example takes a similar approach, but is not packaged to run on Cloud ML. It also
shows an example of using a custom [`Estimator`](https://www.tensorflow.org/api_docs/python/contrib.learn/estimators).

The list of image sources for the images used in the "hugs/no-hugs" training is here:
https://storage.googleapis.com/oscon-tf-workshop-materials/transfer_learning/hugs_photos_sources.csv
