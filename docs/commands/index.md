# PipelineIO Commands
## Installation
```
pip install -U pio-cli
```

## PipelineIO CLI Configuration
| Command              | Description                                               |
| -------------------- | --------------------------------------------------------- |
| pio init-pio         | Initialize PIO CLI                                        |
| pio config           | View current configuration                                |
| pio config-get       | Get value for config key                                  |
| pio config-set       | Set value for config key                                  |

## AI/ML Model Management
| Command              | Description                                               |
| -------------------- | --------------------------------------------------------- |
| pio init-model       | Initialize model for deployment and prediction            |
| pio deploy           | Deploy model to model server                              |
| pio deploy-from-git  | Deploy a model from git                                   |
| pio predict          | Predict with model                                        |
| pio predict-many     | Predict many times (mini load-test)                       |

## New Cluster Creation
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio init-kops      | Initialize cluster (new)                                    |
| pio up             | Start the cluster                                           |

## Existing Cluster Management
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

## Application Management
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

## Job and Workflow Management
| Command            | Description                                                 |
| ------------------ | ----------------------------------------------------------- |
| pio flow           | Start an Airflow job                                        |
| pio submit         | Submit a Spark job                                          |
