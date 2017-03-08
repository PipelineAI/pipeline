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

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCollapserKey;
import com.netflix.hystrix.HystrixCollapserMetrics;
import com.netflix.hystrix.HystrixCollapserProperties;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCollapser;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;
import io.prometheus.client.CollectorRegistry;

/**
 * Implementation of a {@link HystrixMetricsPublisher} for Prometheus Metrics.
 * See <a href="https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring">Hystrix Metrics and Monitoring</a>.
 */
public class HystrixPrometheusMetricsPublisher extends HystrixMetricsPublisher {

    private final HystrixMetricsCollector collector;
    private final boolean exportProperties;

    public HystrixPrometheusMetricsPublisher(String namespace, CollectorRegistry registry, boolean exportProperties) {
        this.collector = new HystrixMetricsCollector(namespace).register(registry);
        this.exportProperties = exportProperties;
    }

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(
            HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey,
            HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
            HystrixCommandProperties properties) {

        return new HystrixPrometheusMetricsPublisherCommand(
                collector, commandKey, commandGroupKey, metrics, circuitBreaker, properties, exportProperties);
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(
            HystrixThreadPoolKey threadPoolKey, HystrixThreadPoolMetrics metrics,
            HystrixThreadPoolProperties properties) {

        return new HystrixPrometheusMetricsPublisherThreadPool(
                collector, threadPoolKey, metrics, properties, exportProperties);
    }

    @Override
    public HystrixMetricsPublisherCollapser getMetricsPublisherForCollapser(
            HystrixCollapserKey collapserKey, HystrixCollapserMetrics metrics,
            HystrixCollapserProperties properties) {

        return new HystrixPrometheusMetricsPublisherCollapser(
                collector, collapserKey, metrics, properties, exportProperties);
    }

    /**
     * Register an instance of this publisher, without a namespace, with the
     * {@link com.netflix.hystrix.strategy.HystrixPlugins} singleton. The publisher
     * registered by this method will register metrics with the default CollectorRegistry
     * and will NOT attempt to export properties.
     *
     * @see CollectorRegistry#defaultRegistry
     */
    public static void register() {
        register(null, CollectorRegistry.defaultRegistry);
    }

    /**
     * Register an instance of this publisher, without a namespace, with the
     * {@link com.netflix.hystrix.strategy.HystrixPlugins} singleton. The publisher
     * registered by this method will NOT attempt to export properties.
     */
    public static void register(CollectorRegistry registry) {
        register(null, registry);
    }

    /**
     * Register an instance of this publisher, for the given namespace, with the
     * {@link com.netflix.hystrix.strategy.HystrixPlugins} singleton. The publisher
     * registered by this method will register metrics with the default CollectorRegistry
     * and will NOT attempt to export properties.
     *
     * @see CollectorRegistry#defaultRegistry
     */
    public static void register(String namespace) {
        register(namespace, CollectorRegistry.defaultRegistry);
    }

    /**
     * Register an instance of this publisher, for the given namespace, with the
     * {@link com.netflix.hystrix.strategy.HystrixPlugins} singleton. The publisher
     * registered by this method will NOT attempt to export properties.
     */
    public static void register(String namespace, CollectorRegistry registry) {
        HystrixPrometheusMetricsPublisher publisher = new HystrixPrometheusMetricsPublisher(namespace, registry, false);
        HystrixPlugins.getInstance().registerMetricsPublisher(publisher);
    }
}
