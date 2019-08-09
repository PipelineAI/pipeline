#!/bin/bash

# Authenticate with Docker Registry
gcloud auth configure-docker --quiet

mlflow run . --backend kubernetes --backend-config kubernetes_config.json
