#!/bin/bash

mlflow run . --backend kubernetes --backend-config kubernetes_config.json
