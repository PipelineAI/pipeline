#!/bin/bash

pipeline model-predict --model-type=http/POST/localhost/6969/tensorflow --model-name=mnist --model-tag=v1 --model-server_url=http://predict-tensorflow-mnist-cpu-v1-gcp.community.pipeline.ai --model-test-request-path=./models/tensorflow/mnist/data/test_request.json --model-test-request-concurrency=1000
