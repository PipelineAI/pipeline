# PipelineIO CLI
## Install CLI
```
pip install pio-cli
```

## Deploy and Test ML/AI Models
### Supported Model Types
* Scikit-Learn
* R
* Spark ML
* TensorFlow
* XGBoost
* Python3
* Java
* PMML
* Ensembles

### Initialize Model 
```
pio init-model --model-server-url <model_server_url> \
               --model-type <model_type> \
               --model-namespace <model_namespace> \
               --model_name <model_name> \
               --model-version <model_version> \
               --model-path /path/to/model \
               --model-test-input-path /path/to/test/inputs
```

### Deploy Model 
```
pio deploy
```

### Predict Model
```
pio predict
```

### Examples
```
git clone https://github.com/fluxcapacitor/source.ml
```

**TensorFlow**

model_type: `tensorflow`

Start Model Server
```
pio start prediction-tensorflow
```

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type tensorflow \
               --model-namespace default \
               --model-name tensorflow_linear \
               --model-version 0 \
               --model-path ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0 \
               --model-test-input-path ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0/test_inputs.txt
```

Deploy Model
```
pio deploy
```
Predict Model
```
pio predict
```

**Scikit-Learn**

model_type: `scikit`

Start Model Server
```
pio start prediction-scikit
```

Initialize Model
```
pio init-model http://your.model.server.com \
               python3 \
               default \
               scikit_linear \
               v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

**Spark ML**

model_type: `spark`


Start Model Server
```
pio start prediction-spark
```

Initialize Model
```
pio init-model http://your.model.server.com \
               spark \
               default \
               spark_airbnb 
               v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```


**Python3**

model_type: `python3`


Start Model Server
```
pio start prediction-python3
```

Initialize Model
```
pio init-model http://your.model.server.com \
               python3 \
               default \
               python3_zscore \
               v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

**PMML**

model_type: `pmml`


Start Model Server
```
pio start prediction-pmml
```

Initialize Model
```
pio init-model http://your.model.server.com \
               pmml \
               default \
               pmml_airbnb \
               v0 \
               ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0 \
               ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```
