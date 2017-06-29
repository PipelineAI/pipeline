# DeployAI 
## Pull Docker Image 
Replace `-cpu` with `-gpu` if applicable.
```
docker pull fluxcapacitor/deploy-predict-cpu:master
```

## Start Model Server
Replace `-cpu` with `-gpu` if applicable.
```
./start-cpu.sh /absolute/path/to/samples/ [model_type] [model_name] 
```

## Install `pio-cli`
```
sudo pip install --upgrade --ignore-installed pio-cli
```

## Deploy Model
```
cd [/path/to/model/directory/]

pio deploy --model_type [model_type] --model_name [model_name]
```

## Predict with Model
```
cd [/path/to/model/directory/]

pio predict --model_type [model_type] --model_name [model_name]
```

## Examples
Replace `-cpu` with `-gpu` if applicable.

### Python3
```
./start-cpu.sh /Users/cfregly/pipeline/deploy.ai/samples/ python3 zscore
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/python3/zscore

pio deploy --model_type python3 --model_name zscore 
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/python3/zscore

pio predict --model_type python3 --model_name zscore
```

### Scikit-Learn
```
./start-cpu.sh /Users/cfregly/pipeline/deploy.ai/samples scikit linear 
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/scikit/linear

pio deploy --model_type scikit --model_name linear --model_path .
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/scikit/linear

pio predict --model_type scikit --model_name linear 
```

### TensorFlow
```
./start-cpu.sh /Users/cfregly/pipeline/deploy.ai/samples
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/tensorflow/linear

pio deploy --model_type tensorflow --model_name linear
```
```
cd /Users/cfregly/pipeline/deploy.ai/samples/tensorflow/linear

pio predict --model_type tensorflow --model_name linear 
```

## REST API
### Deploy Model
```
cd /path/to/model/directory
```
```
tar -cvzf pipeline.tar.gz *
```
```
curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  -F "file=@[path/to/pipeline.tar.gz]" \
  http://[model_server_url]/api/v1/model/deploy/[model_type]/[model_name]
```
### Predict with Model
```
curl -X POST -H "Content-Type: [request_mime_type]" \
  -d '[request_body]' \
  https://[model_server_url]/api/v1/model/predict/[model_type]/[model_name]
```
