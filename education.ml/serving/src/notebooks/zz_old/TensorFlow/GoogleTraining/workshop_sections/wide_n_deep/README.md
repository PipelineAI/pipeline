# Wide & Deep with TensorFlow

This directory contains the code for running a Wide and Deep model. It also runs in Cloud ML, using TensorFlow 0.12.1. This code has been tested on Python 2.7 but should also run on Python 3.5

# About the dataset and model
Wide and deep jointly trains wide linear models and deep neural networks -- to combine the benefits of memorization and generalization for recommender systems. See the [research paper](https://arxiv.org/abs/1606.07792) for more details. The code is based on the [TensorFlow wide and deep tutorial](https://www.tensorflow.org/tutorials/wide_and_deep/).

We will use the [Census Income Dataset](https://archive.ics.uci.edu/ml/datasets/Census+Income) to predict the probability that the individual has an annual income of over 50,000 dollars. This data was extracted from the 1994 US Census by Barry Becker. 

The prediction task is to determine whether a person makes over 50K a year.

# Training and evaluation
This repo presents 3 methods of running the model: locally, on a jupyter notebook, and on Google CloudML.

### Local
`python tensorflow-workshop/workshop_sections/wide_n_deep/widendeep/model.py`

### Jupyter Notebook
`cd workshop_sections/wide_n_deep`

`jupyter notebook`

### Google CloudML
`cd workshop_sections/wide_n_deep`

#### Test it locally:
`gcloud beta ml local train --package-path=widendeep --module-name=widendeep.model`

#### Run it in the cloud:
    gcloud config set compute/region us-central1
    gcloud config set compute/zone us-central1-c
    gcloud config set project gae-load-demo

    export JOB_NAME=widendeep_${USER}_$(date +%Y%m%d_%H%M%S)
    export PROJECT_ID=`gcloud config list project --format "value(core.project)"`
    export BUCKET=gs://${PROJECT_ID}-ml
    export TRAIN_PATH=${BUCKET}/${JOB_NAME}

    gcloud beta ml jobs submit training ${JOB_NAME} --package-path=widendeep --module-name=widendeep.model --staging-bucket=${BUCKET} --region=us-central1 -- --train_dir=${TRAIN_PATH}/train


