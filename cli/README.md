# PipelineIO CLI
## Scikit-Learn
### Deploy Scikit-Learn Model
```
pio deploy --model_server_url=http://prediction-python.demo.pipeline.io:81 --model_namespace=default --model_name=scikit_balancescale --model_version=v2 --model_type=file --model_bundle_path=/root/source.ml/prediction.ml/python/store/default/scikit_balancescale/v2/ --model_file_key=bundle --output_type=json --compression_type=gz
```
### Predict Scikit-Learn Model
```
pio predict --model_server_url=http://prediction-python.demo.pipeline.io --model_namespace=default --model_name=scikit_balancescale --model_version=v0 --input_type=json --output_type=json --input_file=test_inputs.txt
```

## TensorFLow

## Spark ML
