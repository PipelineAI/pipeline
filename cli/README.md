# PipelineIO CLI

## Supported Model Types
* TensorFLow
* Scikit-Learn
* Ad-hoc Python
* Ad-hoc Java
* PMML
* Spark ML
* R
* Ensembles accross all model types


## Install CLI
```
pip install pio-cli
```

## Initialize Cluster (Kubernetes)
```
pio init-cluster --kube-cluster-context <kube-cluster-context> \
                 --kube-namespace <kube-namespace>
```

## Initialize Model 
```
pio init-model --model-server-url <model_server_url> \
               --model-type <model_type> \
               --model-namespace <model_namespace> \
               --model_name <model_name> \
               --model-version <model_version> \
               --model-path /path/to/model \
               --model-test-input-path /path/to/inputs.json
```

## Deploy Model 
```
pio deploy --model_version <version> 
           --model-path /path/to/model
```

## Predict Model
```
pio predict --model_version <version>
            --input-file-path /path/to/inputs
```

## Examples
```
git clone https://github.com/fluxcapacitor/source.ml
```

### TensorFLow
`model_type`: `tensorflow`
```
cd source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0

pio init-model http://your.model.server.com \
               tensorflow \
               default \
               tensorflow_linear 

pio deploy 0 ./

pio predict 0 ./test_inputs.txt
```

### Scikit-Learn
`model_type`: `python3`
```
cd source.ml/prediction.ml/model_store/python3/default/scikit_linear/0

pio init-model http://your.model.server.com \
               python3 \
               default \
               scikit_linear 

pio deploy 0 ./

pio predict 0 ./test_inputs.txt
```

### PMML
`model_type`: `pmml`
```
cd source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/0

pio init-model http://your.model.server.com \
               pmml \
               default \
               pmml_airbnb 

pio deploy 0 ./

pio predict 0 ./test_inputs.txt
```
