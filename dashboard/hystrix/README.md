hystrix-dashboard
=======================================================

[Turbine](https://github.com/Netflix/Turbine/wiki) is a tool for aggregating streams of Server-Sent Event (SSE) JSON data into a single stream. The targeted use case is metrics streams from instances in an SOA being aggregated for dashboards.

For example, Netflix uses [Hystrix](https://github.com/Netflix/Hystrix/wiki) which has a realtime dashboard that uses Turbine to aggregate data from 100s or 1000s of machines.

Here is a snapshot of the dashboard being used to monitor several systems across the company.

### NetflixOSS Hystrix Circuit Breaker Dashboard
* Navigate your browser to
```
http://<hystrix-external-ip>/hystrix-dashboard/
```
* Follow the steps below
![Hystrix Dashboard Turbine Setup](http://pipeline.io/img/hystrix-dashboard-turbine-setup.png)

* Monitor your circuit breakers
![Hystrix Dashboard](http://pipeline.io/img/hystrix-example-600x306.png)
![Hystrix Dashboard](http://pipeline.io/img/hystrix-dashboard-annotated-640x411.png)

User Guide
==============================

hystrix-dashboard can be used to monitor Hystrix circuit breakers for applications running inside Kubernetes pods.

Execute the following `kubectl` command to deploy this hystrix-dashboard docker image as a Kubernetes pod
```
echo '...Dashboard - Turbine...'
kubectl create -f https://raw.githubusercontent.com/fluxcapacitor/pipeline/master/dashboard.ml/turbine-rc.yaml
kubectl create -f https://raw.githubusercontent.com/fluxcapacitor/pipeline/master/dashboard.ml/turbine-svc.yaml
kubectl describe svc turbine
```


Developer Guide
==============================

Run command
```
#!/bin/bash

java -Djava.security.egd=file:/dev/./urandom -jar target/hystrix-dashboard-0.0.1-SNAPSHOT.jar  
```
