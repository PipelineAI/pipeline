## XOR: A minimal training example

This directory contains 3 examples of learning the XOR function with TensorFlow. The most basic solution [xor.py](xor.py) solves the problem using mean squared error as the loss function. [xor_summaries.py](xor_summaries.py) adds additional `summary` operations, for use with TensorBoard, the TensorFlow debugging UI. Output summaries include heatmaps of parameters at the last iteration of training. Loss and accuracy, throughout, as well as histograms of gradients, and an architecture diagram. Finally, [xor_summaries_softmax.py](xor_summaries_softmax.py) substitutes a more traditional cross-entropy loss function using softmax.

### Running locally

To run locally, first choose either:

1. a Google Cloud Storage location you have write access to.
2. A *prexisting* local directory.


```
python xor_summaries.py --output-dir ${OUTPUT_DIR}
```

or if you want to try using the `gcloud beta ml local train` command the following will work:

```
gcloud beta ml local train \
    --package-path xor \
    --module-name xor.xor_summaries \
    -- \
    --output-dir ${OUTPUT_DIR}
```

The `gcloud beta ml local train` command runs your python code locally in an environment that emulates that of the Google Cloud Machine Learning API. This is primarily useful for distributed execution, where the local tool can serve as validation, that your code will run properly.

### Running in the Cloud

To run in the cloud you need a Google Cloud Storage bucket to which summaries can be written. You will also need a bucket where the `gcloud` tool can stage your code for execution e.g. `MY_BUCKET=gs://my-bucket`. This can be the same bucket to which you write summaries.

```
gcloud beta ml jobs submit training myxorjob \
     --module-name xor.xor_summaries \
     --package-path xor \
     --staging-bucket ${MY_BUCKET} \
     -- \
     --output-dir ${OUTPUT_DIR}
```

The service may take some time to start up. Make sure you have authorized the Cloud Machine Learning service account to access your bucket, as described in the installation instructions.

### Using TensorBoard to view summaries

TensorBoard is the web UI which reads binary summary files written by TensorFlow training jobs, and renders them as graphics. TensorBoard ships with TensorFlow and so should be installed in your local environment. Run TensorBoard with:

```
tensorboard --logdir ${OUTPUT_DIR}
```

And it will read from both local and GCS directories. TensorBoard also recursively searches subdirectories of `--logdir` and renders each events file on the same graph allowing you to compare multiple runs of a training job, or different metrics from the same training job.
