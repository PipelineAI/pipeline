#!/bin/bash

pipeline model-predict --model-type=spark --model-name=airbnb --model-tag=master --model-server_url=http://predict-spark-airbnb-cpu-master.community.pipeline.ai --model-test-request-path=./models/spark/airbnb/data/test_request.json --model-test-request-concurrency=1000
