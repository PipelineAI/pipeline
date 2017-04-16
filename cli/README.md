# PipelineIO CLI
## Scikit-Learn
### Deploy Scikit-Learn Model
```
pio deploy --model_server_url=http://prediction-python.demo.pipeline.io:81 --model_namespace=default --model_name=scikit_balancescale --model_version=v0 --model_type=file --model_dir=. --model_file_key=bundle --output_type=json
```
### Predict Scikit-Learn Model
```
pio predict --model_server_url=http://prediction-python.demo.pipeline.io --model_namespace=default --model_name=scikit_balancescale --model_version=v0 --input_type=json --output_type=json --input_file=test_inputs.txt
```

## TensorFLow

## Spark ML
