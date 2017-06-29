# Getting Started with DeployAI 
`model_type`: scikit, tensorflow, python3, spark, xgboost, r, pmml

## Pull Docker Image 
```
docker pull fluxcapacitor/deploy-predict-cpu:master
```

## Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

## Start TensorFlow Model Server
```
export PIO_MODEL_SERVER_URL=http://localhost:6969
export PIO_MODEL_STORE=[/absolute/path/to/this/repo/]/samples
export PIO_MODEL_TYPE=tensorflow
export PIO_MODEL_NAME=linear
```

## CLI Examples
### Install `pio-cli`
```
sudo pip install --upgrade --ignore-installed pio-cli
```

### Deploy Model
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME 

pio deploy --model_server_url $PIO_MODEL_SERVER_URL --model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```

### Predict with Model
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME

pio predict y--model_type $PIO_MODEL_TYPE --model_name $PIO_MODEL_NAME
```

## REST API Examples
### Deploy Model
```
cd $PIO_MODEL_STORE/$PIO_MODEL_TYPE/$PIO_MODEL_NAME

tar -cvzf pipeline.tar.gz *
```
```
curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  -F "file=@pipeline.tar.gz" \
  http://localhost:6969/api/v1/model/deploy/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```
### Predict with Model
```
curl -X POST -H "Content-Type: application/json" \
  -d '' \
  http://localhost:6969/api/v1/model/predict/$PIO_MODEL_TYPE/$PIO_MODEL_NAME
```

## Model Serving Dashboard
```
http://lcoalhost:6969/dashboard
```
