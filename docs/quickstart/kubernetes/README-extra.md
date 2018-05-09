### Split Traffic Between Model Version v1 (50%) and Model Version v2 (50%)
```
pipeline predict-kube-route --model-name=mnist --model-split-tag-and-weight-dict='{"v1":50, "v2":50}' --model-shadow-tag-list='[]'
```
Notes:
* If you specify a model in `--model-shadow-tag-list`, you need to explicitly specify 0% traffic split in `--model-split-tag-and-weight-dict`
* If you see `apiVersion: Invalid value: "config.istio.io/__internal": must be config.istio.io/v1alpha2`, you need to [remove the existing route rules](#clean-up) and re-create them with this command.

### Shadow Traffic from Model Version v1 (100% Live) to Model Version v1 (0% Live, Only Shadow Traffic)
```
pipeline predict-kube-route --model-name=mnist --model-split-tag-and-weight-dict='{"v1":100, "v2":0}' --model-shadow-tag-list='["v2]'
```
Notes:
* If you specify a model in `--model-shadow-tag-list`, you need to explicitly specify 0% traffic split in `--model-split-tag-and-weight-dict`
* If you see `apiVersion: Invalid value: "config.istio.io/__internal": must be config.istio.io/v1alpha2`, you need to [remove the existing route rules](#clean-up) and re-create them with this command.

### View Route Rules
```
kubectl get routerules1

### EXPECTED OUTPUT ###
NAME                            KIND
predict-mnist-dashboardstream   RouteRule.v1alpha2.config.istio.io
predict-mnist-denytherest       RouteRule.v1alpha2.config.istio.io
predict-mnist-invocations       RouteRule.v1alpha2.config.istio.io
predict-mnist-metrics           RouteRule.v1alpha2.config.istio.io
predict-mnist-ping              RouteRule.v1alpha2.config.istio.io
predict-mnist-prometheus        RouteRule.v1alpha2.config.istio.io
```

**REST-Based Http Load Test**
```
pipeline predict-http-test --endpoint-url=http://$PREDICT_HOST:$PREDICT_PORT/predict/mnist/invocations --test-request-path=./tensorflow/mnist-v/input/predict/test_request.json --test-request-concurrency=1000
```
Notes:
* Make sure the Host IP is accessible.  You may need to use `127.0.0.1` or `localhost`
* If you see `no healthy upstream` or `502 Bad Gateway`, just wait 1-2 mins for the model servers to startup.
* If you see a `404` error related to `No message found /predict/mnist/invocations`, the route rules above were not applied.
* See [Troubleshooting](/docs/troubleshooting) for more debugging info.

**Expected Output**
* You should see a 100% traffic to Model Version v1, however Model Version b is receiving a "best effort" amount of live traffic. (See Dashboards to verify.)

[**Mnist v1**](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-v1)
```
('{"variant": "mnist-v1-tensorflow-tfserving-cpu", "outputs":{"classes": [3], '
 '"probabilities": [[2.353575155211729e-06, 3.998300599050708e-06, '
 '0.00912125688046217, 0.9443341493606567, 3.8211437640711665e-06, '
 '0.0003914404078386724, 5.226673920333269e-07, 2.389515998402203e-07, '
 '0.04614224657416344, 8.35775360030766e-09]]}}')
 
Request time: 36.414 milliseconds
``` 

### Remove Pipeline"I Traffic Routes
```
kubectl delete routerule predict-mnist-dashboardstream
kubectl delete routerule predict-mnist-denytherest
kubectl delete routerule predict-mnist-invocations
kubectl delete routerule predict-mnist-metrics
kubectl delete routerule predict-mnist-ping
kubectl delete routerule predict-mnist-prometheus
```
