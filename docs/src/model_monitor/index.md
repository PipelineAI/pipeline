# Dashboards and Visualizations 
Metrics, dashboards, and visualizations are native to PipelineIO.  

We believe that you should have full insight into everything deployed to production.

PipelineIO provides the usual dashboards for real-time system metrics including memory usage, disk I/O and network throughput, CPU and GPU utilization of your model training and deployment activities.

In addition, PipelineIO provides dashboards for real-time prediction metrics including accuracy, latency, and throughput of your models in production.

## Example Dashboards

### Stabilize the Cluster
Unhealthy or latent model servers may open a [circuit](https://www.infoq.com/interviews/Building-Resilient-Systems-Michael-Nygard), respond with a suitable fallback, and allow the cluster to stabilize.

![Stabilize the Cluster](/img/hystrix-example-600x306.png)

### Optimize Performance
Large batch sizes provide higher throughput at the expense of latency.  PipelineIO dynamically configures the system to find the proper balance.

![Optimize Performance](/img/istio-dashboard-600x284.png)

### Control Latency with Timeouts
High latency may lead to unhealthy model servers if left unbounded.  All PipelineIO service calls are bound with timeouts.

### Monitor and Alert
High resource utilization - beyond container and physical node limits - will certainly degrade performance.  PipelineIO monitors all system resources.

### Scale Dynamically 
All PipelineIO services support auto-scaling across federated cloud and on-premise environments.

{!contributing.md!}
