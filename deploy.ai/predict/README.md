# Getting Started 

## Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

## Pull Docker Image 
```
docker pull fluxcapacitor/deploy-predict-cpu:master
```

## Start Model Server
`model_type`: scikit, tensorflow, python3, spark, xgboost, r, pmml
```
export PIO_MODEL_STORE=[/absolute/path/to/this/repo]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```
```
docker run --name=deploy-predict-cpu -itd -m 4G -p 6969:6969 -p 7070:7070 -p 80:80 -p 10254:10254 -p 9876:9876 -p 9040:9040 -p 9090:9090 -p 3000:3000 -v $PIO_MODEL_STORE:/root/model_store -e "PIO_MODEL_TYPE=$PIO_MODEL_TYPE" -e "PIO_MODEL_NAME=$PIO_MODEL_NAME" fluxcapacitor/deploy-predict-cpu:master
```

## REST API
### Deploy Model
```
export PIO_MODEL_SERVER_URL=http://localhost:6969
export PIO_MODEL_STORE=[/absolute/path/to/this/repo]/deploy.ai/predict/samples
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
  http://$PIO_MODEL_SERVER_URL/api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

### Predict Model
```
export PIO_MODEL_SERVER_URL=http://localhost:6969
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"x_observed":1.5}' \
  http://$PIO_MODEL_SERVER_URL/api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

## WebUI 
```
http://$PIO_MODEL_SERVER_URL/
```

## Dashboard
```
http://$PIO_MODEL_SERVER_URL/dashboard
```

## CLI Example
### Install `pio-cli`
```
sudo pip install --upgrade --ignore-installed pio-cli
```

### Deploy Model
```
export PIO_MODEL_SERVER_URL=http://localhost:6969
export PIO_MODEL_STORE=[/absolute/path/to/this/repo]/deploy.ai/predict/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear

cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME

pio deploy --model_server_url $PIO_MODEL_SERVER_URL --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```

### Predict with Model
```
export PIO_MODEL_SERVER_URL=http://localhost:6969
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear

cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME

pio predict --model_server_url $PIO_MODEL_SERVER_URL --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```
