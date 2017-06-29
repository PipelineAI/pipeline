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
`model_type`: scikit, tensorflow, python3, spark, xgboost, r, pmml
```
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```
```
docker run --name=deploy-predict-cpu -itd -m 4G \
  -p 6969:6969 -p 7070:7070 -p 10254:10254 -p 9876:9876 -p 9040:9040 -p 9090:9090 -p 3000:3000 \
  -v $PIO_MODEL_STORE:/root/model_store \
  -e "PIO_MODEL_TYPE=$PIO_MODEL_TYPE" -e "PIO_MODEL_NAME=$PIO_MODEL_NAME" \
  fluxcapacitor/deploy-predict-cpu:master
```

## REST API
### Deploy Model
```
export PIO_MODEL_SERVER_HOST=localhost
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME

tar -cvzf pipeline.tar.gz *
```
```
curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  -F "file=@pipeline.tar.gz" \
  http://$PIO_MODEL_SERVER_HOST:6969/api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

### Predict Model
```
export PIO_MODEL_SERVER_HOST=localhost
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"x_observed":1.5}' \
  http://$PIO_MODEL_SERVER_HOST:6969/api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

## WebUI
The following is under heavy construction.  Ignore this for now. 
```
http://$PIO_MODEL_SERVER_HOST:6969/
```

## Dashboards (Grafana)
Username/Password: admin/admin

Pre-built dashboards are coming soon.
```
http://$PIO_MODEL_SERVER_HOST:3000/
```
Note:  Use `http://$PIO_MODEL_SERVER_HOST:9090` for the Prometheus data source within your Grafana Dashboard.


## Command Line API (CLI) 
### Install `pio-cli`
```
sudo pip install --upgrade --ignore-installed pio-cli
```

### Deploy Model
```
export PIO_MODEL_SERVER_HOST=localhost
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/pipeline]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
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
export PIO_MODEL_SERVER_HOST=localhost
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
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
