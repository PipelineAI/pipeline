![PipelineAI Logo](http://pipeline.ai/assets/img/logo/pipelineai-split-black-258x62.png)

### Install Docker for Mac with Kubernetes 
```
https://www.docker.com/docker-mac
```

**Enable Kubernetes**

![Docker for Desktop Kubernetes](http://pipeline.ai/assets/img/docker-desktop-kubernetes.png?classes=border,shadow)

**Minimum Version**
* Edge (Not Stable)
* 17.12.0-ce
* Kubernetes v1.8.2

![Docker for Desktop Kubernetes About](http://pipeline.ai/assets/img/docker-desktop-kubernetes-about.png?classes=border,shadow)

**Minimum Docker System Requirements**
* 8GB
* 4 Cores

![Docker for Desktop Kubernetes Config](http://pipeline.ai/assets/img/docker-desktop-kubernetes-config.png?classes=border,shadow)

**Configure Kubernetes CLI for Local Kubernetes Cluster**
```
kubectl config use-context docker-for-desktop
```

### Install PipelineAI CLI
* If you're having trouble, see our [Troubleshooting](/docs/troubleshooting) Guide.
```
pip install cli-pipeline==1.5.19 --ignore-installed --no-cache -U 
```

### Pull PipelineAI Sample Models
```
git clone https://github.com/PipelineAI/models
```

### Build Models 025 and 050 (TensorFlow-based)

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-server-build --model-name=mnist --model-tag=025 --model-type=tensorflow --model-path=./tensorflow/mnist-0.025/model
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-server-build --model-name=mnist --model-tag=050 --model-type=tensorflow --model-path=./tensorflow/mnist-0.050/model
```

### Push Models 025 and 050 to Docker Repo
Notes:  
* This can be any Docker Repo including DockerHub and internal repos

Defaults
* `--image-registry-url`:  docker.io
* `--image-registry-repo`:  pipelineai

[CPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-cpu)
```
pipeline predict-server-push --model-name=mnist --model-tag=cpu --image-registry-url=<your-registry> --image-registry-repo=<your-repo>
```

[GPU](https://github.com/PipelineAI/models/tree/master/tensorflow/mnist-gpu)
```
pipeline predict-server-push --model-name=mnist --model-tag=gpu --image-registry-url=<your-registry> --image-registry-repo=<your-repo>
```



[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-server-push --model-name=mnist --model-tag=025
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-server-push --model-name=mnist --model-tag=050
```

### Install Istio Service Mesh CLI
```
curl -L https://git.io/getLatestIstio | sh -
```

### Deploy Istio Service Mesh Components**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/istio.yaml
```

### Deploy Models 025 and 050 (TensorFlow-based)
_Make sure you install Istio.  See above!_

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
pipeline predict-kube-start --model-name=mnist --model-tag=025
```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
```
pipeline predict-kube-start --model-name=mnist --model-tag=050
```

### View Running Pods
```
kubectl get pod

### EXPECTED OUTPUT###
NAME                            READY     STATUS    RESTARTS   AGE
predict-mnist-025-...-...       2/2       Running   0          5m
predict-mnist-050-...-...       2/2       Running   0          5m
```

### Split Traffic Between Model 025 (50%) and Model 050 (50%)
```
pipeline predict-kube-route --model-name=mnist --model-tag-and-weight-dict='{"025":50, "050":50}'
```

### View Route Rules
```
kubectl get routerules

### EXPECTED OUTPUT ###
NAME                            KIND
predict-mnist-dashboardstream   RouteRule.v1alpha2.config.istio.io
predict-mnist-denytherest       RouteRule.v1alpha2.config.istio.io
predict-mnist-invocations       RouteRule.v1alpha2.config.istio.io
predict-mnist-metrics           RouteRule.v1alpha2.config.istio.io
predict-mnist-ping              RouteRule.v1alpha2.config.istio.io
predict-mnist-prometheus        RouteRule.v1alpha2.config.istio.io
```

### Run Load Test on Models 025 and 050
The input data is the same across both models, so we just use the data from [mnist-0.025](https://github.com/PipelineAI/models/blob/6c9a2a0c6f132e07fad54783ae71180a01eb146a/tensorflow/mnist-0.025/input/predict/test_request.json).
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=1000
```

**Expected Output**
[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.025) (Learning Rate = 0.025)
```
('{"variant": "mnist-025-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, '
 '0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, '
 '0.017851119861006737, 0.006651509087532759, 0.07679300010204315, '
 '0.001954273320734501]}}')
 
 Request time: 36.414 milliseconds
 ```

[CPU](https://github.com/PipelineAI/models/tree/f559987d7c889b7a2e82528cc72d003ef3a34573/tensorflow/mnist-0.050) (Learning Rate = 0.050)
 ```
('{"variant": "mnist-050-tensorflow-tfserving-cpu", "outputs":{"outputs": '
 '[0.11128010600805283, 1.4478532648354303e-05, 0.43401211500167847, '
 '0.06995825469493866, 0.002808149205520749, 0.2786771059036255, '
 '0.01785111241042614, 0.006651511415839195, 0.07679297775030136, '
 '0.001954274717718363]}}')

Request time: 29.968 milliseconds
 ```

### Scale Model 025 to 2 Replicas
Scale the Model Server
```
pipeline predict-kube-scale --model-name=mnist --model-tag=025 --replicas=2
```

Verify Scale Event
```
kubectl get pod

### EXPECTED OUTPUT###

NAME                            READY     STATUS    RESTARTS   AGE
predict-mnist-025-...-...       2/2       Running   0          8m
predict-mnist-025-...-...       2/2       Running   0          10m
predict-mnist-050-...-...       2/2       Running   0          10m
```

### Install Dashboards
**Prometheus**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/addons/prometheus.yaml
```
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=prometheus -o jsonpath='{.items[0].metadata.name}') 9090:9090 &   
```
```
http://localhost:9090/graph 
```

**Grafana**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/addons/grafana.yaml
```
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=grafana -o jsonpath='{.items[0].metadata.name}') 3000:3000 &
```
```
http://localhost:3000/dashboard/db/istio-dashboard
```

**Service Graph**
```
kubectl apply -f https://raw.githubusercontent.com/istio/istio/0.4.0/install/kubernetes/addons/servicegraph.yaml
```
```
kubectl -n istio-system port-forward $(kubectl -n istio-system get pod -l app=servicegraph -o jsonpath='{.items[0].metadata.name}') 8088:8088 &   
```
```
http://localhost:8088/dotviz
```

### Re-Run Load Test on Models 025 and 050
_You may need to wait 30 secs for the 2nd replica to start up completely._
```
pipeline predict-kube-test --model-name=mnist --test-request-path=./tensorflow/mnist-0.025/input/predict/test_request.json --test-request-concurrency=1000
```

### TODO: Predict with REST API
Use the REST API to POST a JSON document representing the number 2.

![MNIST 2](http://pipeline.ai/assets/img/mnist-2-100x101.png)
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"image": [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.05098039656877518, 0.529411792755127, 0.3960784673690796, 0.572549045085907, 0.572549045085907, 0.847058892250061, 0.8156863451004028, 0.9960784912109375, 1.0, 1.0, 0.9960784912109375, 0.5960784554481506, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7882353663444519, 0.11764706671237946, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.988235354423523, 0.7921569347381592, 0.9450981020927429, 0.545098066329956, 0.21568629145622253, 0.3450980484485626, 0.45098042488098145, 0.125490203499794, 0.125490203499794, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.32156863808631897, 0.9921569228172302, 0.803921639919281, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6352941393852234, 0.9921569228172302, 0.803921639919281, 0.24705883860588074, 0.3490196168422699, 0.6509804129600525, 0.32156863808631897, 0.32156863808631897, 0.1098039299249649, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.007843137718737125, 0.7529412508010864, 0.9921569228172302, 0.9725490808486938, 0.9686275124549866, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.8274510502815247, 0.29019609093666077, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2549019753932953, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.847058892250061, 0.027450982481241226, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5921568870544434, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.7333333492279053, 0.44705885648727417, 0.23137256503105164, 0.23137256503105164, 0.4784314036369324, 0.9921569228172302, 0.9921569228172302, 0.03921568766236305, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5568627715110779, 0.9568628072738647, 0.7098039388656616, 0.08235294371843338, 0.019607843831181526, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.43137258291244507, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.15294118225574493, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08627451211214066, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1882353127002716, 0.9921569228172302, 0.9921569228172302, 0.46666669845581055, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6705882549285889, 0.9921569228172302, 0.9921569228172302, 0.12156863510608673, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.2392157018184662, 0.9647059440612793, 0.9921569228172302, 0.6274510025978088, 0.003921568859368563, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.08235294371843338, 0.44705885648727417, 0.16470588743686676, 0.0, 0.0, 0.2549019753932953, 0.9294118285179138, 0.9921569228172302, 0.9333333969116211, 0.27450981736183167, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.4941176772117615, 0.9529412388801575, 0.0, 0.0, 0.5803921818733215, 0.9333333969116211, 0.9921569228172302, 0.9921569228172302, 0.4078431725502014, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.7411764860153198, 0.9764706492424011, 0.5529412031173706, 0.8784314393997192, 0.9921569228172302, 0.9921569228172302, 0.9490196704864502, 0.43529415130615234, 0.007843137718737125, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.6235294342041016, 0.9921569228172302, 0.9921569228172302, 0.9921569228172302, 0.9764706492424011, 0.6274510025978088, 0.1882353127002716, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.18431372940540314, 0.5882353186607361, 0.729411780834198, 0.5686274766921997, 0.3529411852359772, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]}' \
  http://localhost:80/invocations \
  -w "\n\n"

### Expected Output ###
{"variant": "mnist-025-tensorflow-tfserving-cpu", "outputs":{"outputs": [0.11128007620573044, 1.4478533557849005e-05, 0.43401220440864563, 0.06995827704668045, 0.0028081508353352547, 0.27867695689201355, 0.017851119861006737, 0.006651509087532759, 0.07679300010204315, 0.001954273320734501]}}

### Formatted Output
Digit  Confidence
=====  ==========
0      0.0022526539396494627
1      2.63791100074684e-10
2      0.4638307988643646      <-- Prediction
3      0.21909376978874207
4      3.2985670372909226e-07
5      0.29357224702835083 
6      0.00019597385835368186
7      5.230629176367074e-05
8      0.020996594801545143
9      5.426473762781825e-06
```
Notes:
* Instead of `localhost`, you may need to use `192.168.99.100` or another IP/Host that maps to your local Docker host.  This usually happens when using Docker Quick Terminal on Windows 7.


### Clean Up

### Uninstall PipelineAI Models
```
pipeline predict-kube-stop --model-name=mnist --model-tag=025
```
```
pipeline predict-kube-stop --model-name=mnist --model-tag=050
```

### Remove PipelineAI Traffic Routes
```
kubectl delete routerule predict-mnist-dashboardstream
kubectl delete routerule predict-mnist-denytherest
kubectl delete routerule predict-mnist-invocations
kubectl delete routerule predict-mnist-metrics
kubectl delete routerule predict-mnist-ping
kubectl delete routerule predict-mnist-prometheus
```

### Uninstall Istio Components
```
kubectl delete namespace istio-system
```
