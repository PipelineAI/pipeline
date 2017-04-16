# PipelineIO CLI
## Scikit-Learn
### Deploy Scikit-Learn Model
```
pio deploy_with_dir --model_server_url=http://prediction-python.demo.pipeline.io:81 --model_namespace=default --model_name=scikit_balancescale --model_version=v0 --model_type=file --model_dir=. --return_type=json
```
### Predict Scikit-Learn Model
```
pio predict_with_string --model_server_url=http://prediction-python.demo.pipeline.io --model_namespace=default --model_name=scikit_balancescale --model_version=v1 --input_type=json --return_type=json --input_str='{"feature0":0, "feature1":1, "feature2":2, "feature3":3}'
```

## TensorFLow

## Spark ML
