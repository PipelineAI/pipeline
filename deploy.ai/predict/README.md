# Getting Started 

## Pull Docker Image 
```
docker pull fluxcapacitor/deploy-predict-cpu:master
```

## Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

## Start Model Server
`model_type`: [scikit](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/scikit/), [tensorflow](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/tensorflow/), [python3](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/python3/), [keras](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/keras/), spark, xgboost, r, [pmml](https://github.com/fluxcapacitor/pipeline/tree/master/deploy.ai/predict/samples/pmml/)
```
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=scikit
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
```
docker run --name=deploy-predict-cpu -itd -m 4G \
  -p 6969:6969 -p 9090:9090 -p 3000:3000 \
  -v $PIO_MODEL_STORE:/root/model_store \
  -e "PIO_MODEL_TYPE=$PIO_MODEL_TYPE" -e "PIO_MODEL_NAME=$PIO_MODEL_NAME" \
  fluxcapacitor/deploy-predict-cpu:master
```

**Wait for the container to fully start by using the following command:**
```
docker logs -f deploy-predict-cpu
```

## REST API
### Deploy Model
```
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=scikit
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```
```
tar -cvzf pipeline.tar.gz *
```

**Note:  The Docker container must be fully started and settled before running the following command:**
```
curl -i -X POST -H "Transfer-Encoding: chunked" \
  -F "file=@pipeline.tar.gz" \
  http://$PIO_MODEL_SERVER_HOST:6969/api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAME \
  -w "\n\n"
```

### Predict Model
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
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
export PIO_MODEL_SERVER_HOST=localhost
```
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
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
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
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
