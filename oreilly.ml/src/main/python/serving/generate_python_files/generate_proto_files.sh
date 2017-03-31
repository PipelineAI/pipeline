#!/bin/bash

set -x
set -e

python -m grpc.tools.protoc -I./ --python_out=.. --grpc_python_out=.. ./*.proto
