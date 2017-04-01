#!/bin/bash

# Args:
#   $1:  zone: (us-west1-b (230), us-east1-d (50), europe-west1-b (50), asia-east1-a (50))
#   $2:  index

gcloud compute instances delete pipeline-oreilly-gpu-$1-$2 
