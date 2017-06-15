# Deploy Your Model
![Deploy from Jupyter Notebook](/img/deploy-model-jupyter-740x510.png)

![Deploy Spark ML Model to Production](/img/deploy-ml-model-to-production.png)

## Supported Model Types
### Scikit-Learn
![Scikit-Learn](/img/scikit-logo-277x150.png)

### R
![R](/img/r-logo-280x212.png)

### Spark ML
![Spark ML](/img/spark-logo-254x163.png)

### TensorFlow
![TensorFlow](/img/tensorflow-logo-202x168.png)

### XGBoost
![XGBoost](/img/xgboost-logo-280x120.png)

### Python 
![Python](/img/python-logo-184x180.png)

### Java
![Java](/img/java-logo-300x168.png)

### Recommendations
![Recommendations](/img/recommendations-logo-280x196.png)

### Key-Value (Redis)
![Redis](/img/redis-logo-300x100.png)

### Key-Value (Cassandra)
![Cassandra](/img/cassandra-logo-279x187.png)

### PMML
![PMML](/img/pmml-logo-210x96.png)

### Custom Ensembles
![Custom Ensembles](/img/ensemble-logo-285x125.png)


## Examples
```
git clone https://github.com/fluxcapacitor/source.ml
```

Note: You can use the same `model-server-url` from the the PipelineAI [Community Edition](http://community.pipeline.io).  See the Community Edition example [notebooks](http://community.pipeline.io) for complete, end-to-end examples.

### TensorFlow
![TensorFlow](/img/tensorflow-logo-202x168.png)

model_type: `tensorflow`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type tensorflow \
               --model-namespace default \
               --model-name tensorflow_linear \
               --model-version 0 \
               --model-path ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0 \
               --model-test-request-path ./source.ml/prediction.ml/model_store/tensorflow/default/tensorflow_linear/0/test_inputs.txt
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
![Scikit-Learn](/img/scikit-logo-277x150.png)

model_type: `scikit`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type scikit \
               --model-namespace default \
               --model-name scikit_linear \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0 \
               --model-test-request-path ./source.ml/prediction.ml/model_store/scikit/default/scikit_linear/v0/test_inputs.txt
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
![Spark ML](/img/spark-logo-254x163.png)

model_type: `spark`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type spark \
               --model-namespace default \
               --model-name spark_airbnb 
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0 \
               --model-test-request-path ./source.ml/prediction.ml/model_store/spark/default/spark_airbnb/v0/test_inputs.txt
```

Deploy Model (CLI)
```
pio deploy
```

Deploy Model (REST)
```
import requests

deploy_url = 'http://your.model.server.com/api/v1/model/deploy/spark/default/spark_airbnb/v0'

files = {'file': open('model.spark', 'rb')}

response = requests.post(deploy_url, files=files)

print("Success!\n\n%s" % response.text)
```

Predict Model (CLI)
```
pio predict
```

Predict Model (REST)
```
import json

data = {"bathrooms":2.0,
        "bedrooms":2.0,
        "security_deposit":175.00,
        "cleaning_fee":25.0,
        "extra_people":1.0,
        "number_of_reviews": 2.0,
        "square_feet": 250.0,
        "review_scores_rating": 2.0,
        "room_type": "Entire home/apt",
        "host_is_super_host": "0.0",
        "cancellation_policy": "flexible",
        "instant_bookable": "1.0",
        "state": "CA" }

json_data = json.dumps(data)

with open('test_inputs.json', 'wt') as fh:
    fh.write(json_data)
```
```
predict_url = 'http://your.model.server.com/api/v1/model/predict/spark/default/spark_airbnb/v0'

headers = {'content-type': 'application/json'}

response = requests.post(predict_url,
                         data=json_data,
                         headers=headers)

print("Response:\n\n%s" % response.text)
```

### Python3
![Python](/img/python-logo-184x180.png)

model_type: `python3`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type python3 \
               --model-namespace default \
               --model-name python3_zscore \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0 \
               --model-test-request-path ./source.ml/prediction.ml/model_store/python3/default/python3_zscore/v0/test_inputs.txt
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
![PMML](/img/pmml-logo-210x96.png)

model_type: `pmml`

Initialize Model
```
pio init-model --model-server-url http://your.model.server.com \
               --model-type pmml \
               --model-namespace default \
               --model-name pmml_airbnb \
               --model-version v0 \
               --model-path ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0 \
               --model-test-request-path ./source.ml/prediction.ml/model_store/pmml/default/pmml_airbnb/v0/test_inputs.txt
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
PipelineAI uses python-based Airflow for pipeline workflow management.
```
pio flow
```

## Upgrade
PipelineAI supports rolling upgrades.
```
pio upgrade
```

## Canary Deploy
PipelineAI supports various canary deployment strategies including traffic-splitting and traffic-shadowing.
```
pio canary
```
### Traffic Splitting

### Traffic Shadowing

## Rollback
PipelineAI supports rolling back to any previous revision.
```
pio rollback --revision=1
```

{!contributing.md!}
