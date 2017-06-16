#!/bin/bash

# https://github.com/tobegit3hub/deep_recommend_system/tree/master/java_predict_client

set -x
set -e

python -m grpc.tools.protoc -I./ --python_out=.. --grpc_python_out=.. ./*.proto
