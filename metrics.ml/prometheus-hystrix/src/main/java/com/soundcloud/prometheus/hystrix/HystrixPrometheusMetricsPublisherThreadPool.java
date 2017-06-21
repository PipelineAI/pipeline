/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soundcloud.prometheus.hystrix;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Implementation of a {@link HystrixMetricsPublisherThreadPool} for Prometheus Metrics.
 * See <a href="https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring">Hystrix Metrics and Monitoring</a>.
 */
public class HystrixPrometheusMetricsPublisherThreadPool implements HystrixMetricsPublisherThreadPool {

    private final Map<String, String> labels;
    private final boolean exportProperties;

    private final HystrixThreadPoolMetrics metrics;
    private final HystrixThreadPoolProperties properties;
    private final HystrixMetricsCollector collector;

    public HystrixPrometheusMetricsPublisherThreadPool(
            HystrixMetricsCollector collector, HystrixThreadPoolKey key, HystrixThreadPoolMetrics metrics,
            HystrixThreadPoolProperties properties, boolean exportProperties) {

        this.metrics = metrics;
        this.collector = collector;
        this.properties = properties;
        this.exportProperties = exportProperties;
        this.labels = Collections.singletonMap("pool_name", key.name());
    }

    @Override
    public void initialize() {
        String currentStateDoc = "Current state of thread-pool partitioned by pool_name.";
        addGauge("thread_active_count", currentStateDoc, metrics::getCurrentActiveCount);
        addGauge("completed_task_count", currentStateDoc, metrics::getCurrentCompletedTaskCount);
        addGauge("largest_pool_size", currentStateDoc, metrics::getCurrentLargestPoolSize);
        addGauge("total_task_count", currentStateDoc, metrics::getCurrentTaskCount);
        addGauge("queue_size", currentStateDoc, metrics::getCurrentQueueSize);

        String rollDoc = "Rolling count partitioned by pool_name.";
        addGauge("rolling_max_active_threads", rollDoc, metrics::getRollingMaxActiveThreads);
        addGauge("rolling_count_threads_executed", rollDoc, metrics::getRollingCountThreadsExecuted);

        String totalDoc = "Cumulative count partitioned by pool_name.";
        addGauge("count_threads_executed", totalDoc, metrics::getCumulativeCountThreadsExecuted);

        if (exportProperties) {
            String doc = "Configuration property partitioned by pool_name.";
            addGauge("property_value_core_pool_size", doc, () -> properties.coreSize().get());
            addGauge("property_value_keep_alive_time_in_minutes", doc, () -> properties.keepAliveTimeMinutes().get());
            addGauge("property_value_queue_size_rejection_threshold", doc, () -> properties.queueSizeRejectionThreshold().get());
            addGauge("property_value_max_queue_size", doc, () -> properties.maxQueueSize().get());
        }
    }

    private void addGauge(String metric, String helpDoc, Callable<Number> value) {
        collector.addGauge("hystrix_thread_pool", metric, helpDoc, labels, value);
    }
}
