#!/bin/bash
# Copyright 2016 Google Inc. All Rights Reserved.
#
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

# This sample assumes you're already setup for using CloudML.  If this is your
# first time with the service, start here:
# https://cloud.google.com/ml/docs/how-tos/getting-set-up

# TODO: usage


if [ -z "$1" ]
  then
    echo "No GCS_PATH supplied"
    exit 1
fi

if [ -z "$2" ]
  then
    echo "No VERSION_NAME supplied"
    exit 1
fi

if [ -z "$3" ]
  then
    declare -r MODEL_NAME=hugs
  else
    declare -r MODEL_NAME=$3
fi

declare -r PROJECT=$(gcloud config list project --format "value(core.project)")
declare -r VERSION_NAME=$2
declare -r GCS_PATH=$1

echo
echo "Using GCS_PATH: " $GCS_PATH
echo "Using VERSION_NAME: " $VERSION_NAME
echo "Using MODEL NAME: " $MODEL_NAME
set -v

# Tell CloudML about a new type of model coming.  Think of a "model" here as
# a namespace for deployed Tensorflow graphs.  This will give an error
# if the model already exists.
gcloud beta ml models create "$MODEL_NAME"

set -e

# Each unique Tensorflow graph--with all the information it needs to execute--
# corresponds to a "version".  Creating a version actually deploys our
# Tensorflow graph to a Cloud instance, and gets it ready to serve (predict).
# This will give an error if the version name already exists.
gcloud beta ml versions create "$VERSION_NAME" \
  --model "$MODEL_NAME" \
  --origin "${GCS_PATH}/training/model"

# Models do not need a default version, but it's a great way to move
# your production
# service from one version to another with a single gcloud command.
gcloud beta ml versions set-default "$VERSION_NAME" --model "$MODEL_NAME"

# Now, generate a request json file with test image(s). If you trained a model
# with the 'flower' images, edit the image list appropriately.
python images_to_json.py -o request.json prediction_images/knife.jpg prediction_images/puppy2.jpg prediction_images/hedgehog.jpg

set +v
echo
echo "You can run this command to see all your models:"
echo "gcloud beta ml models list"
echo
echo "Next, from the command line, run the following.  It might take a"
echo "few moments for the prediction service to spin up."
echo
echo "gcloud beta ml predict --model ${MODEL_NAME} --json-instances request.json"



