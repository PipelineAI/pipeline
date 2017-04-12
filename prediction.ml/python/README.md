Each model server can contain 1 `<namespace>/<model_name>` combo.  

`namespace`:  "default" by default

`model_name`:  anything you'd like

`version`:  unique version of model (ie. v0, myModelA, yourModel_9)

## Predict with JSON
```
curl -X POST -H "Content-Type: application/json" -d '<json-data>' http://<prediction-python-service>/<namespace>/<model_name>/<version>
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
