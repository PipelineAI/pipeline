# Dashboards and Visualizations 
Metrics, dashboards, and visualizations are native to PipelineIO.  

We believe that you should have full insight into everything deployed to production.

PipelineIO provides the usual dashboards for real-time system metrics including memory usage, disk I/O and network throughput, CPU and GPU utilization of your model training and deployment activities.

In addition, PipelineIO provides dashboards for real-time model server metrics including latency, throughput, and health of your models in production.

## Example Dashboards

### Latency
Higher latency may lead to an unhealthy model server if left unbounded.

### Memory Usage
Higher memory usage beyond physical container or node limits may degrade performance.

### GPU Utilization
Higher, consistent utilization is preferred.  You want to saturate your cores, but not oversaturate them.

### Request Batch Size
Larger batch sizes usually provide higher throughput.

### Health
Unhealthy or latent services may open a [circuit](https://www.infoq.com/interviews/Building-Resilient-Systems-Michael-Nygard).  This causes the circuit to return a degraded, fallback response.

![Model Health](/img/hystrix-example-600x306.png)

{!contributing.md!}
