#!/bin/bash -e

curl  -d 'json={"data":{"ndarray":[[0]]}}' http://0.0.0.0:4444/predict
