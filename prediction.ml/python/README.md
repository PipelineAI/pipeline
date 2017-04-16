`model_namespace`:  "default" by default

`model_name`:  anything you'd like

`model_version`:  unique version of model (ie. v0, myModelA, yourModel_9)

## `pio-cli`
```
pip install pio-cli
```
### Deploy New Version of Model Bundle
```
pio deploy --model_server_url=http://<prediction-python-service>:81 --model_namespace=default --model_name=scikit_balancescale --model_version=v1 --model_type=file --model_bundle_path=/path/to/<model_namespace>/<model_name>/<model_version>/ --model_file_key=bundle --output_type=json --compression_type=gz
```
### Predict
```
curl -X POST -H "Content-Type: application/json" -d '<json-data>' http://<prediction-python-service>/<model_namespace>/<model_name>/<model_version>
```

## REST
### Deploy New Version of Model Bundle
Create Model Bundle
```
cd <inside_model_dir>

tar -czvf bundle.tar.gz  <-- this can be named anything, but must match below
```

Upload New Bundle
`bundle.tar.gz`:  must match filename above
```
curl -X POST -F bundle=@/path/to/bundle.tar.gz http://<prediction-python-service>:81/<model_namespace>/<model_name>/<model_version>
```

### Predict
```
pio predict --model_server_url=http://<prediction-python-service> --model_namespace=default --model_name=scikit_balancescale --model_version=v1 --input_type=json --input_filename=/path/to/<model_namespace>/<model_name>/<model_version>/test_inputs.txt --output_type=json
```
