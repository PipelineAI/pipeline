# Getting Started 

## Pull Docker Image 
```
docker pull fluxcapacitor/deploy-predict-cpu:master
```

## Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

## Build Model Server
`model_type`: [scikit](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/scikit/), [tensorflow](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/tensorflow/), [python3](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/python3/), [keras](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/keras/), spark, xgboost, r, [pmml](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/pmml/)
```
export PIO_MODEL_TYPE=scikit
export PIO_MODEL_NAME=linear

docker build -t fluxcapacitor/deploy-predict-$PIO_MODEL_TYPE-$PIO_MODEL_NAME-cpu:master \
  --build-arg model_type=$PIO_MODEL_TYPE \
  --build-arg model_name=$PIO_MODEL_NAME -f Dockerfile.cpu .
```

## Predict 
```
export PIO_MODEL_TYPE=scikit
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
**Note:  The Docker container must be fully started and settled before running the following command:**
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"feature0": 0.03807590643342410180}' \
  http://$PIO_MODEL_SERVER_HOST:6969/api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAME \
  -w "\n\n"
```

## WebUI
The following is under heavy construction.  Ignore for now. 
```
http://$PIO_MODEL_SERVER_HOST:6969/
```

## Dashboards (Grafana)
Username/Password: **admin**/**admin**

Pre-built dashboards coming soon.

Note:  Use `http://$PIO_MODEL_SERVER_HOST:9090` for the Prometheus data source within your Grafana Dashboard.
```
http://$PIO_MODEL_SERVER_HOST:3000/
```

## Command Line API (CLI) 
### Install `pio-cli`
```
sudo pip install --upgrade --ignore-installed pio-cli
```

### Deploy Model
```
export PIO_MODEL_STORE_PATH=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
```
cd $PIO_MODEL_STORE_PATH/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```
```
pio deploy --model_server_url http://$PIO_MODEL_SERVER_HOST:6969 \
  --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```

### Predict with Model
```
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
```
cd $PIO_MODEL_STORE_PATH/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

Predict an Individual Request
```
pio predict --model_server_url http://$PIO_MODEL_SERVER_HOST:6969 \
  --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```

Predict 100 Concurrent Requests
```
pio predict --concurrency 100 --model_server_url http://$PIO_MODEL_SERVER_HOST:6969 \
  --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```
