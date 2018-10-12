# predict-python : Python Tornado REST API
Build Status by Branch
* master: [![Build Status](https://fluxcapacitor.jenkins.com/buildStatus/icon?job=fluxcapacitor/predict-python/master)](https://fluxcapacitor.jenkins.com/job/fluxcapacitor/job/predict-python/job/master/)

# Getting Started 

Getting Started.

## Clone this Repo
```
git clone git@github.com:fluxcapacitor/predict-python.git
```

## Setup `pipeline-cli` 
Note:  This requires Python3.
```
pip3 install --ignore-installed --no-cache -U pipeline-cli
```
```
pipeline init
```

## Build Your Model into the Model Server

model-store : [s3:///fluxcapacitor-us-east-1/model-store](https://s3.console.aws.amazon.com/s3/buckets/fluxcapacitor-us-east-1/model-store/?region=us-east-1&tab=overview)
`model_type`: scikit, tensorflow, python3, keras, spark, xgboost, r

```
cd predict-python
```
```
pipeline model-init --model-type=scikit \
                    --model-name=linear \
```
```
pipeline model-build
```

## Start Model Server with Your Model
```
pipeline model-start
```
Note:  If you see `docker: Error response from daemon: ... failed: port is already allocated.`, you likely have another Docker container running.  Use `docker ps` to find the container-id, then `docker rm -f <container-id>` to remove the other Docker container.

## Predict 
### CLI
```
pipeline model-predict --model-server-url=http://localhost:6969 \
                       --model-test-request-path=data/test_request.json
```

Perform 50 Predictions in Parallel
```
pipeline model-predict --model-server-url=http://localhost:6969 \
                  --model-test-request-path=data/test_request.json \
                  --concurrency=50
```

### REST API
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"feature0": 0.03807590643342410180}' \
  http://localhost:6969/api/v1/model/predict/scikit/linear \
  -w "\n\n"
```
