# PipelineIO CLI

## Scikit-Learn
### Initialize Model CLI
```
pio model-init --model_server_url <model_server_url> --model_namespace <model_namespace> --model_name <model_name>
```
### Deploy Scikit-Learn Model
```
pio model-deploy --model-version v0 --model-bundle-path /path/to/<model_namespace>/<model_name>/<model_version>/ 
```

### Predict Scikit-Learn Model
```
pio model-predict --model-version v0 --model-input-file-path /path/to/test_inputs.txt
```

## TensorFLow
Documentation Coming Soon!

## Spark ML
Documentation Coming Soon!

## R
Documentation Coming Soon!

## PMML
