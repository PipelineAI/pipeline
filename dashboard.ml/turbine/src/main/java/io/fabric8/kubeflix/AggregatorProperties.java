package io.fabric8.kubeflix;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "turbine.aggregator")
public class AggregatorProperties {

    Map<String, List<String>> clusters;

    public AggregatorProperties() {
    }

    public AggregatorProperties(Map<String, List<String>> clusters) {
        this.clusters = clusters;
    }

    public Map<String, List<String>> getClusters() {
        return clusters;
    }

    public void setClusters(Map<String, List<String>> clusters) {
        this.clusters = clusters;
    }
}
