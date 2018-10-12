# predict : Python Tornado REST API
Build Status by Branch
* master: [![Build Status](https://fluxcapacitor.jenkins.com/buildStatus/icon?job=fluxcapacitor/predict/master)](https://fluxcapacitor.jenkins.com/job/fluxcapacitor/job/predict/job/master/)

# Getting Started 

Getting Started.

## Clone this Repo
```
git clone git@github.com:fluxcapacitor/predict.git
```

## Setup `pipeline-cli` 
Note:  This requires Python3.
```
pip3 install --ignore-installed --no-cache -U pipeline-ai-cli
```
```
pipeline init
```

## Build Your Model into the Model Server
`model_type`: [scikit](https://github.com/fluxcapacitor/predict/tree/master/models/scikit/), 
[tensorflow](https://github.com/fluxcapacitor/predict/tree/master/models/tensorflow/), 
[python3](https://github.com/fluxcapacitor/predict/tree/master/models/python3/), 
[keras](https://github.com/fluxcapacitor/predict/tree/master/models/keras/), spark, xgboost, r
```
cd predict
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

## WebUI
The following is under heavy construction.  Ignore for now. 
```
http://localhost:6969/
```

## Dashboards (Grafana)
Username/Password: **admin**/**admin**

Pre-built dashboards coming soon.

Note:  Use `http://localhost:9090` for the Prometheus data source within your Grafana Dashboard.
```
http://localhost:3000/
```
