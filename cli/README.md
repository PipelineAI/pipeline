# PipelineIO CLI

## Scikit-Learn

### Deploy Scikit-Learn Model
```
pio model_deploy --model_server_url=<model_server_url>:81 --model_namespace=<model_namespace> --model_name=<model_name> --model_version=<model_version> --model_bundle_path=/path/to/<model_namespace>/<model_name>/<model_version>/
```

### Predict Scikit-Learn Model
```
pio model_predict --model_server_url=<model_server_url> --model_namespace=<model_namespace> --model_name=<model_name> --model_version=<model_version> --input_file_path=/path/to/inputs.json
```

## TensorFLow
Documentation Coming Soon!

## Spark ML
Documentation Coming Soon!

## R
Documentation Coming Soon!

## PMML
