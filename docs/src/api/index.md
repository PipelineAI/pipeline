# PipelineIO APIs
## Command Line Interface (CLI)
### Installation
```
pip install -U pio-cli
```

You can view the commands supported by the CLI using just `pio`.
```
pio
```

### PipelineIO CLI Configuration
| Command              | Description                                               |
| -------------------- | --------------------------------------------------------- |
| pio init-pio         | Initialize PIO CLI                                        |
| pio config           | View current configuration                                |
| pio config-get       | Get value for config key                                  |
| pio config-set       | Set value for config key                                  |

### AI/ML Model Deployment and Prediction
| Command              | Description                                               |
| -------------------- | --------------------------------------------------------- |
| pio init-model       | Initialize model for deployment and prediction            |
| pio deploy           | Deploy model to model server                              |
| pio deploy-from-git  | Deploy a model from git                                   |
| pio predict          | Predict with model                                        |
| pio predict-many     | Predict many times (mini load-test)                       |

### New Cluster Creation
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio init-kops      | Initialize cluster (new)                                    |
| pio up             | Start the cluster                                           |

### Cluster Management
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio init-cluster   | Initialize cluster (existing)                               |
| pio cluster        | View the current cluster                                    |
| pio clusters       | View all federated, hybrid, cross-cloud, on-premise cluster |
| pio join           | Join a federeated, hybrid, cross-cloud, on-premise cluster  |
| pio nodes          | View cluster nodes                                          |
| pio instancegroups | View instance groups                                        |
| pio volumes        | View volumes and volume claims                              |
| pio secrets        | View secrets                                                |
| pio maps           | View config maps                                            |
| pio system/top     | View system resource utilization (RAM, CPU) for an app      |

### Application Management
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio apps           | Retrieve available and running apps                         |
| pio start          | Start an app                                                |
| pio stop           | Stop/kill an app                                            |
| pio scale          | Scale an app (ie. Spark Worker or TensorFlow Model Server)  |
| pio upgrade        | Upgrade an app                                              |
| pio rollback       | Rollback an app                                             |
| pio canary         | Canary a new version of an app                              |
| pio logs           | View app logs                                               |
| pio connect/shell  | Connect to an app to run commands at the terminal           |
| pio proxy/tunnel   | Create a proxy/tunnel to a private app in your cluster      |
| pio system/top     | Get system resource utilization (RAM, CPU) for an app       |

### Job and Workflow Management
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio flow           | Start an Airflow job                                        |
| pio submit         | Submit a Spark job                                          |

## REST API
More Documentation Coming Soon!

### AI/ML Model Deploy and Predict
**Deploy Model**
```
import requests

deploy_url = 'http://<pipelineio-model-server>/api/v1/model/deploy/spark/default/airbnb/v0'

files = {'file': open('airbnb.parquet', 'rb')}

response = requests.post(deploy_url, files=files)

print("Success!\n\n%s" % response.text)
```

**Predict with Model**
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
predict_url = 'http://<pipelineio-model-server>/api/v1/model/predict/spark/default/airbnb/v0'

headers = {'content-type': 'application/json'}

response = requests.post(predict_url, 
                         data=json_data, 
                         headers=headers)

print("Response:\n\n%s" % response.text)
```

{!contributing.md!}
