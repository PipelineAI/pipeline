Initialize a PipelineIO model.

### Usage
```bash

pio init-model --model-server-url <model_server_url> \
               --model-type <model_type> \
               --model-namespace <model_namespace> \
               --model_name <model_name> \
               --model-version <model_version> \
               --model-path /path/to/model \
               --model-test-input-path /path/to/test/inputs

```

### Parameters 
| Name                         | Default | Description                                                        |
| ---------------------------- | ------- | ------------------------------------------------------------------ |
| model-server-url             |         | Http endpoint URL hosting your model                               |
| model-type                   |         | See Model Types section                                            |
| model-namespace              |         | Unique namespace for your models                                   |
| model-name                   |         | Unique name for your model within your model namespace             |
| model-version                |         | Unique version with model name and namespace (ie. 1, v0, git hash) |
       
### Description
Creates or updates the `~/.pio/config` with the given parameters.

This command also creates a `.pioignore` file in the `model-path` directory. 
Any files and directories you do not want PipelineIO to track can be added to this file. 
When you deploy your model on PipelineIO, these files will not be uploaded. 
More details on how the `.pioignore` file works is available [here](../home/pio_ignore).

### Model Types
* scikit
* R
* spark
* tensorflow
* xgboost
* python3
* java
* pmml
* ensemble

### Examples
Clone the source repo
```
git clone https://github.com/fluxcapacitor/source.ml
```

TensorFlow
```
pio init-model http://your.model.server.com \
               tensorflow \
               default \
               tensorflow_linear \
               0 \
               ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0 \
               ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0/test_inputs.txt

pio deploy

pio predict
```

Scikit-Learn
```
pio init-model http://your.model.server.com \
               python3 \
               default \
               scikit_linear \
               v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0 \
               ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0/test_inputs.txt

pio deploy

pio predict
```

Spark ML
```
pio init-model http://your.model.server.com \
               spark \
               default \
               spark_airbnb 
               v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0 \
               ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0/test_inputs.txt

pio deploy

pio predict
```

Python3
```
pio init-model http://your.model.server.com \
               python3 \
               default \
               python3_zscore \
               v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0 \
               ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0/test_inputs.txt

pio deploy

pio predict
```

{!contributing.md!}
