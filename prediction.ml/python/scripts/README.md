Assuming `prediction.ml/python/scripts/` is on the PATH

## Setup Model Serving Environment (Conda)
This creates an environment called `${PIO_MODEL_NAMESPACE}_${PIO_MODEL_NAME}_${PIO_MODEL_VERSION}
```
PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> setup
```

## Start Model Serving Environment
```
$PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> PIO_MODEL_FILENAME=<model-filename.pkl> PIO_MODEL_SERVER_PORT=9876 start
```
