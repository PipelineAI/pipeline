#!/bin/bash

pipeline model-predict --model-type=spark --model-name=airbnb --model-tag=v2 --model-server_url=http://predict-spark-airbnb-cpu-v2-gcp.community.pipeline.ai --model-test-request-path=./models/spark/airbnb/data/test_request.json --model-test-request-concurrency=1000
