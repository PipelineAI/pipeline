Each model server can contain 1 `<namespace>/<model_name>` combo.  

Versions are incremental according to 

The latest version is used unless specifically set in the `python-deploy.yaml`.

## Predict with JSON
```
curl -X POST -H "Content-Type: application/json" -d '<json-data>' http://<prediction-python-service>
```

## Upload New Version of Model Bundle
### Create Model Bundle
```
cd <inside_model_dir>

tar -czvf bundle.tar.gz  <-- this can be named anything, but must match below
```

### Upload New Bundle
`bundle.tar.gz`:  must match filename above

`namespace`:  "default" by default

`model_name`:  anything you'd like

`version`:  unique version of model
```
curl -X POST -F bundle=@bundle.tar.gz http://<prediction-python-service>:81/<namespace>/<model_name>/<version>
```
