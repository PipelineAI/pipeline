# Initialize a PipelineIO model.

## Usage
```bash

pio init-model --model-server-url <model_server_url> \
               --model-type <model_type> \
               --model-namespace <model_namespace> \
               --model_name <model_name> \
               --model-version <model_version> \
               --model-path /path/to/model \
               --model-test-input-path /path/to/test/inputs

```

## Parameters 
| Name                         | Description                                                        |
| ---------------------------- | ------------------------------------------------------------------ |
| model-server-url             | Model Server URL hosting your models                               |
| model-type                   | See Model Types section.                                           |
| model-namespace              | Unique namespace for your models                                   |
| model-name                   | Unique name for your model within your model namespace             |
| model-version                | Unique version with model name and namespace (ie. 1, v0, git hash) |
| model-path                   | /path/to/model/  Contents will be tar.gz'd and uploaded.           | 
| model-test-input-path        | /path/to/test/inputs.txt  File used for testing the model locally. |
       
## Description
This command will create or update the `~/.pio/config` file and prepare you for model deploying and predicting.

## Model Types
* scikit
* R
* spark
* tensorflow
* xgboost
* python3
* java
* pmml
* ensemble

## Examples
Clone the Example [source.ml](https://github.com/fluxcapacitor/source.ml/prediction.ml) Model Store Repo
```
git clone https://github.com/fluxcapacitor/source.ml
```

### TensorFlow
[Initialize](init-model.md) Model
```
pio init-model http://prediction-tensorflow.demo.pipeline.io \
               tensorflow \
               default \
               tensorflow_linear \
               0 \
               ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0 \
               ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0/test_inputs.txt
```

[Deploy](deploy.md) the Model
```
pio deploy
```

[Predict](predict.md) with Model
```
pio predict
```

### Scikit-Learn
[Initialize](init-model.md) Model
```
pio init-model http://prediction-tensorflow.demo.pipeline.io \
               python3 \
               default \
               scikit_linear \
               v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0/test_inputs.txt
```

[Deploy](deploy.md) the Model
```
pio deploy
```

[Predict](predict.md) with Model
```
pio predict
```

### Spark ML
[Initialize](init-model.md) Model
```
pio init-model http://prediction-tensorflow.demo.pipeline.io \
               spark \
               default \
               spark_airbnb 
               v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0/test_inputs.txt
```

[Deploy](deploy.md) the Model
```
pio deploy
```

[Predict](predict.md) with Model
```
pio predict
```

### Python3
[Initialize](init-model.md) Model
```
pio init-model http://prediction-tensorflow.demo.pipeline.io \
               python3 \
               default \
               python3_zscore \
               v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0/test_inputs.txt
```

[Deploy](deploy.md) the Model
```
pio deploy
```

[Predict](predict.md) with Model
```
pio predict
```

{!contributing.md!}
