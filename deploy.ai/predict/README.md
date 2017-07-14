# Getting Started 

## Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

## Setup `pio-cli` 
Note:  This requires Python3.
```
pip3 install --ignore-installed --no-cache -U pio-cli
```
```
pio init
```

## Build Your Model into the Model Server
`model_type`: [scikit](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/scikit/), [tensorflow](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/tensorflow/), [python3](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/python3/), [keras](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/keras/), spark, xgboost, r, [pmml](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/pmml/)
```
cd pipeline/deploy.ai/predict
```
```
pio model-init --model-type=scikit \
               --model-name=linear 
```
```
pio model-build
```

## Start Model Server with Your Model
```
pio model-start
```
Note:  If you see `docker: Error response from daemon: ... failed: port is already allocated.`, you likely have another Docker container running.  Use `docker ps` to find the container-id, then `docker rm -f <container-id>` to remove the other Docker container.

## Predict 
### CLI
```
pio model-predict --model-server-url=http://localhost:6969 \
                  --model-test-request-path=data/test_request.json
```

Perform 50 Predictions in Parallel
```
pio model-predict --model-server-url=http://localhost:6969 \
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
