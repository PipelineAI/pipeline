# PipelineAI APIs

## Install Pre-Requisites
### Docker
* Install [Docker](https://www.docker.com/community-edition#/download)

### Python3
* Install [Miniconda](https://conda.io/docs/install/quick.html) with Python3 Support

## Install PipelineAI CLI
### Installation
Note: This command line interface requires Python3 and Docker.  See Pre-Requisites above.
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

## Deploy Model Locally
Supported Model Types:
* [scikit](https://github.com/fluxcapacitor/pipeline/tree/master/predict/samples/scikit/)
* [tensorflow](https://github.com/fluxcapacitor/pipeline/tree/master/predict/samples/tensorflow/)
* [python3](https://github.com/fluxcapacitor/pipeline/tree/master/predict/samples/python3/) 
* [keras](https://github.com/fluxcapacitor/pipeline/tree/master/predict/samples/keras/)
* spark
* xgboost
* r
* [pmml](https://github.com/fluxcapacitor/pipeline/tree/master/predict/samples/pmml/)

### Clone this Repo
```
git clone https://github.com/fluxcapacitor/pipeline
```

Enter the `predict/` directory within the cloned repo above.
```
cd pipeline/predict
```

### Initialize Model
```
pipeline model-init --model-type=tensorflow \
                    --model-name=mnist
```

### Build and Package Model Source
This command will build the model source code into a runnable Docker image.
```
pipeline model-package --package-type=docker
```

### Train and Deploy Model Locally
This command will start the model train, optimize, and deploy as a runnable Docker image from above.
```
pipeline model-start --memory=2G
```

Monitor the train, optimize, and deployment logs.
```
pipeline package-logs
```

### View Model Train, Optimize, and Deploy Dashboard
Navigate to the following:
```
http://localhost:6333
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
