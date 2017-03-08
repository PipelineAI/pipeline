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

# Now that we are set up, we can start processing some images. We'll show how
# easy it is to swap in another set of images, instead of using the original
# 'flowers' set.
# For this script, we'll use a fun set of images labeled 'hugs' or 'not-hugs',
# with the images classified as to whether you would want to 'hug' the thing
# shown.

# Usage: hugs_preproc.sh gs://your-bucket-name
# substituting 'your-bucket-name' with the name of the GCS bucket to use.

# gs://aju-vtests2-ml-amy
if [ -z "$1" ]
  then
    echo "No bucket supplied."
    exit 1
fi

declare -r PROJECT=$(gcloud config list project --format "value(core.project)")
declare -r JOB_ID="hugs_${USER}_$(date +%Y%m%d_%H%M%S)"
declare -r DFJOB_ID="hugs-$(date +%Y%m%d-%H%M%S)"
declare -r BUCKET=$1
declare -r GCS_PATH="${BUCKET}/${USER}/${JOB_ID}"
declare -r DICT_FILE=gs://oscon-tf-workshop-materials/transfer_learning/cloudml/hugs_photos/dict.txt

echo
echo "Using job id: " $JOB_ID
echo "Using GCS_PATH: " $GCS_PATH
echo "Using eval output path: " "${GCS_PATH}/preproc/eval"
echo "Using train output path: " "${GCS_PATH}/preproc/train"

set -v -e

# We'll use Dataflow (Beam) to do our preprocessing. The preprocessing will use
# the trained Inception 3 model, and run our images through it to derive so-
# called 'bottleneck' embeddings for the images. The embedding info will be
# saved in the form of TFRecords, in GCS. We'll be able to use these records to
# train our own model.

# It takes a bit of time to preprocess all the images. For the workshop, we will
# also have pre-generated records that you can point to during the training
# phase.


# If run in the cloud, these calls are asynchronous (by default).
# You can track job progress in the Dataflow console.
# The following uses just 3 cores each, but you can edit to use more
# if your project quota is sufficient to do so.
python trainer/preprocess.py \
  --input_dict "$DICT_FILE" \
  --input_path "gs://oscon-tf-workshop-materials/transfer_learning/cloudml/hugs_photos/eval_data.csv" \
  --output_path "${GCS_PATH}/preproc/eval" \
  --job_name "${DFJOB_ID}e" \
  --num_workers 3 \
  --cloud

python trainer/preprocess.py \
  --input_dict "$DICT_FILE" \
  --input_path "gs://oscon-tf-workshop-materials/transfer_learning/cloudml/hugs_photos/train_data.csv" \
  --output_path "${GCS_PATH}/preproc/train" \
  --job_name "${DFJOB_ID}t" \
  --num_workers 3 \
  --cloud

set +v
echo
echo "Generated job id: " $JOB_ID
echo "Using GCS_PATH: " $GCS_PATH
echo "Using eval output path: " "${GCS_PATH}/preproc/eval"
echo "Using train output path: " "${GCS_PATH}/preproc/train"
echo "Dataflow jobs are running at ${DFJOB_ID}e and ${DFJOB_ID}t"
