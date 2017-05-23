# Deploy Model
![Deploy from Jupyter Notebook](deploy-model-jupyter-740x510.png)

![Deploy Spark ML Model to Production](/img/deploy-ml-model-to-production.png)

## Supported Model Types
* Scikit-Learn
* R
* Spark ML
* TensorFlow
* XGBoost
* Python3
* Java
* PMML
* Ensembles

## Examples
```
git clone https://github.com/fluxcapacitor/source.ml
```

### TensorFlow

model_type: `tensorflow`

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

### Scikit-Learn

model_type: `scikit`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type scikit \
               --model-namespace default \
               --model-name scikit_linear \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0 \
               --model-test-input-path ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

### Spark ML

model_type: `spark`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type spark \
               --model-namespace default \
               --model-name spark_airbnb 
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0 \
               --model-test-input-path ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

### Python3

model_type: `python3`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type python3 \
               --model-namespace default \
               --model-name python3_zscore \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0 \
               --model-test-input-path ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

### PMML

model_type: `pmml`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type pmml \
               --model-namespace default \
               --model-name pmml_airbnb \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0 \
               --model-test-input-path ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0/test_inputs.txt
```

Deploy Model
```
pio deploy
```

Predict Model
```
pio predict
```

## Deployment Workflow
PipelineIO uses python-based Airflow for pipeline workflow management.
```
pio flow
```

## Upgrade
PipelineIO supports rolling upgrades.
```
pio upgrade
```

## Canary Deploy
PipelineIO supports various canary deployment strategies including traffic-splitting and traffic-shadowing.
```
pio canary
```
### Traffic Splitting

### Traffic Shadowing

## Rollback
PipelineIO supports rolling back to any previous revision.
```
pio rollback --revision=1
```

{!contributing.md!}
