Assuming `prediction.ml/python/scripts/` is on the PATH

## Scikit-Learn Model Server
### Setup Model Server Environment (Conda)
This creates an environment called `${PIO_MODEL_NAMESPACE}_${PIO_MODEL_NAME}_${PIO_MODEL_VERSION}
```
PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> create_environment
```

### Start Model Server
```
PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> PIO_MODEL_FILENAME=<model-filename.pkl> PIO_MODEL_SERVER_PORT=9876 spawn_model_server
```

### Delete Model Server Environment
```
PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> delete_environment
```

## Bundle Server (to Upload Model Bundles)
# TODO: Remove this in favor of Scala verion
### Start Bundle Server
```
PIO_MODEL_NAMESPACE=<namespace> PIO_MODEL_NAME=<model_name> PIO_MODEL_VERSION=<version> PIO_BUNDLE_SERVER_PORT=8000 spawn_bundle_server
```

### Test Bundle Server
```
curl -X POST -F file=@bundle.tar.gz http://127.0.0.1:8000
```

### Verify Successful Bundle Upload
```
ls -l $STORE_HOME/$PIO_MODEL_NAMESPACE/$PIO_MODEL_NAME/$PIO_MODEL_VERSION/
```
