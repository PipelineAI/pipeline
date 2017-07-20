# PipelineAI APIs

## Pre-requisites
* Install [Docker for Mac](https://www.docker.com/docker-mac)
* Install [Docker for Windows](https://www.docker.com/docker-windows)

## Command Line Interface (CLI)
### Installation
Note: This cli requires Python3.
```
pip3 install --ignore-installed --no-cache -U pipeline-ai-cli
```

### Explore CLI
You can view the commands supported by the CLI using just `pipeline`.
```
pipeline
```

### Initialize CLI
```
pipeline init
```

## Build Model into a Docker Image
Supported Model Types:
* Scikit-Learn
* R
* Spark ML
* TensorFlow
* XGBoost
* Python3
* Java
* PMML
* Ensembles

### Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

### Initialize Model
```
cd pipeline/predict
```
```
pipeline model-init --model-type=scikit \
                    --model-name=linear
```

### Build Model into a Docker Image
```
pipeline model-build
```

## Predict 
### CLI
Perform Single Prediction
```
pipeline model-predict --model-server-url=http://localhost:6969 \
                       --model-test-request-path=data/test_request.json
```

Perform 100 Concurrent Predictions
```
pipeline model-predict --model-server-url=http://localhost:6969 \
                       --model-test-request-path=data/test_request.json \
                       --concurrency=100
```

### REST API
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"inputs": 0.03807590643342410180}' \
  http://localhost:6969/api/v1/model/predict/tensorflow/mnist \
  -w "\n\n"
```

## Model System and Prediction Metrics
Username/Password: **admin**/**admin**

Pre-built dashboards coming soon.

Note:  Use `http://localhost:9090` for the Prometheus data source within your Grafana Dashboard.
```
http://localhost:3000/
```

{!contributing.md!}
