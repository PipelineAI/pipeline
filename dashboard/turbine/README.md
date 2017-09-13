turbine-server
=======================================================
               

[Turbine](https://github.com/Netflix/Turbine/wiki) is a tool for aggregating streams of Server-Sent Event (SSE) JSON data into a single stream. The targeted use case is metrics streams from instances in an SOA being aggregated for dashboards.

For example, Netflix uses [Hystrix](https://github.com/Netflix/Hystrix/wiki) which has a realtime dashboard that uses Turbine to aggregate data from 100s or 1000s of machines.

Here is a snapshot of the dashboard being used to monitor several systems across the company.

![Hytrix Dashboard](https://raw.githubusercontent.com/fluxcapacitor/pipeline/master/dashboard/turbine/NetflixDash.jpg)

User Guide
==============================

turbine-server can be used to monitor Hystrix circuit breakers for applications running inside Kubernetes pods.


Developer Guide
==============================

Run command
```
#!/bin/bash

java -Djava.security.egd=file:/dev/./urandom -jar target/turbine-server-0.0.1-SNAPSHOT.jar  
```
