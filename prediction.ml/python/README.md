## Key Parameters
`model_namespace`:  arbitrary namespace; "default" by default

`model_name`:  anything you'd like, but usually the logical name of your model; ie. census, iris

`model_version`:  version of model; unique within <model_namespace>/<model_name>; ie. v0, myModelA, yourModel_9

`model_bundle_path`:  path to parent directory containing all model assets

## PipelineIO Command Line Interface (CLI) 
```
pip install pio-cli
```

### Deploy New Version of Model Bundle
```
pio model_deploy --model_server_url=<model_server_url>:81 --model_namespace=<model_namespace> --model_name=<model_name> --model_version=<model_version> --model_bundle_path=/path/to/<model_namespace>/<model_name>/<model_version>/
```

### Predict 
```
pio model_predict --model_server_url=<model_server_url> --model_namespace=<model_namespace> --model_name=<model_name> --model_version=<model_version> --input_file_path=/path/to/inputs.json
```

## REST
### Deploy New Version of Model Bundle
Manually Create Model Bundle (`tar.gz` file)
```
cd /path/to/<model_namespace>/<model_name>/<model_version>

tar -czvf bundle.tar.gz . 
```
_Note:  The `bundle.tar.gz` file above ^^ can be named anything, but must match bundle filename below._

Upload New Version of Model Bundle
```
curl -X POST -F bundle=@/path/to/bundle.tar.gz http://<prediction-python-service>:81/<model_namespace>/<model_name>/<model_version>
```

### Predict
```
curl -X POST -H "Content-Type: application/json" -d '<json-data>' http://<prediction-python-service>/<model_namespace>/<model_name>/<model_version>
```
