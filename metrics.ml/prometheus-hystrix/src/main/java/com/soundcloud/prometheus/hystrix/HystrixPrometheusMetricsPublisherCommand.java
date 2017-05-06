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
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * Implementation of a {@link HystrixMetricsPublisherCommand} for Prometheus Metrics.
 * See <a href="https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring">Hystrix Metrics and Monitoring</a>.
 */
public class HystrixPrometheusMetricsPublisherCommand implements HystrixMetricsPublisherCommand {

    private final Map<String, String> labels;
    private final boolean exportProperties;

    private final HystrixCommandMetrics metrics;
    private final HystrixCircuitBreaker circuitBreaker;
    private final HystrixCommandProperties properties;
    private final HystrixMetricsCollector collector;

    public HystrixPrometheusMetricsPublisherCommand(
            HystrixMetricsCollector collector, HystrixCommandKey commandKey, HystrixCommandGroupKey commandGroupKey,
            HystrixCommandMetrics metrics, HystrixCircuitBreaker circuitBreaker,
            HystrixCommandProperties properties, boolean exportProperties) {

        this.labels = new TreeMap<>();
        this.labels.put("command_group", (commandGroupKey != null) ? commandGroupKey.name() : "default");
        this.labels.put("command_name", commandKey.name());

        this.exportProperties = exportProperties;

        this.circuitBreaker = circuitBreaker;
        this.properties = properties;
        this.collector = collector;
        this.metrics = metrics;
    }

    @Override
    public void initialize() {
        String circuitDoc = "Current status of circuit breaker: 1 = open, 0 = closed.";
        addGauge("is_circuit_breaker_open", circuitDoc, () -> booleanToNumber(circuitBreaker.isOpen()));

        String errorsDoc = "Error percentage derived from current metrics.";
        addGauge("error_percentage", errorsDoc, () -> metrics.getHealthCounts().getErrorPercentage());

        String permitsDoc = "The number of executionSemaphorePermits in use right now.";
        addGauge("execution_semaphore_permits_in_use", permitsDoc, metrics::getCurrentConcurrentExecutionCount);

        createCumulativeCountForEvent("count_emit", HystrixRollingNumberEvent.EMIT);
        createCumulativeCountForEvent("count_success", HystrixRollingNumberEvent.SUCCESS);
        createCumulativeCountForEvent("count_failure", HystrixRollingNumberEvent.FAILURE);
        createCumulativeCountForEvent("count_timeout", HystrixRollingNumberEvent.TIMEOUT);
        createCumulativeCountForEvent("count_bad_requests", HystrixRollingNumberEvent.BAD_REQUEST);
        createCumulativeCountForEvent("count_short_circuited", HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createCumulativeCountForEvent("count_thread_pool_rejected", HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createCumulativeCountForEvent("count_semaphore_rejected", HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createCumulativeCountForEvent("count_fallback_emit", HystrixRollingNumberEvent.FALLBACK_EMIT);
        createCumulativeCountForEvent("count_fallback_success", HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createCumulativeCountForEvent("count_fallback_failure", HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createCumulativeCountForEvent("count_fallback_rejection", HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createCumulativeCountForEvent("count_exceptions_thrown", HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createCumulativeCountForEvent("count_responses_from_cache", HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createCumulativeCountForEvent("count_collapsed_requests", HystrixRollingNumberEvent.COLLAPSED);

        createRollingCountForEvent("rolling_count_emit", HystrixRollingNumberEvent.EMIT);
        createRollingCountForEvent("rolling_count_success", HystrixRollingNumberEvent.SUCCESS);
        createRollingCountForEvent("rolling_count_failure", HystrixRollingNumberEvent.FAILURE);
        createRollingCountForEvent("rolling_count_timeout", HystrixRollingNumberEvent.TIMEOUT);
        createRollingCountForEvent("rolling_count_bad_requests", HystrixRollingNumberEvent.BAD_REQUEST);
        createRollingCountForEvent("rolling_count_short_circuited", HystrixRollingNumberEvent.SHORT_CIRCUITED);
        createRollingCountForEvent("rolling_count_thread_pool_rejected", HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
        createRollingCountForEvent("rolling_count_semaphore_rejected", HystrixRollingNumberEvent.SEMAPHORE_REJECTED);
        createRollingCountForEvent("rolling_count_fallback_emit", HystrixRollingNumberEvent.FALLBACK_EMIT);
        createRollingCountForEvent("rolling_count_fallback_success", HystrixRollingNumberEvent.FALLBACK_SUCCESS);
        createRollingCountForEvent("rolling_count_fallback_failure", HystrixRollingNumberEvent.FALLBACK_FAILURE);
        createRollingCountForEvent("rolling_count_fallback_rejection", HystrixRollingNumberEvent.FALLBACK_REJECTION);
        createRollingCountForEvent("rolling_count_exceptions_thrown", HystrixRollingNumberEvent.EXCEPTION_THROWN);
        createRollingCountForEvent("rolling_count_responses_from_cache", HystrixRollingNumberEvent.RESPONSE_FROM_CACHE);
        createRollingCountForEvent("rolling_count_collapsed_requests", HystrixRollingNumberEvent.COLLAPSED);

        String latencyExecDoc = "Rolling percentiles of execution times for the "
                + "HystrixCommand.run() method (on the child thread if using thread isolation).";

        addGauge("latency_execute_mean", latencyExecDoc, metrics::getExecutionTimeMean);

        createExcecutionTimePercentile("latency_execute_percentile_5", 5, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_25", 25, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_50", 50, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_75", 75, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_90", 90, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_99", 99, latencyExecDoc);
        createExcecutionTimePercentile("latency_execute_percentile_995", 99.5, latencyExecDoc);

        String latencyTotalDoc = "Rolling percentiles of execution times for the "
                + "end-to-end execution of HystrixCommand.execute() or HystrixCommand.queue() until "
                + "a response is returned (or ready to return in case of queue(). The purpose of this "
                + "compared with the latency_execute* percentiles is to measure the cost of thread "
                + "queuing/scheduling/execution, semaphores, circuit breaker logic and other "
                + "aspects of overhead (including metrics capture itself).";

        addGauge("latency_total_mean", latencyTotalDoc, metrics::getTotalTimeMean);

        createTotalTimePercentile("latency_total_percentile_5", 5, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_25", 25, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_50", 50, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_75", 75, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_90", 90, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_99", 99, latencyTotalDoc);
        createTotalTimePercentile("latency_total_percentile_995", 99.5, latencyTotalDoc);

        if (exportProperties) {
            String propDesc = "These informational metrics report the "
                    + "actual property values being used by the HystrixCommand. This is useful to "
                    + "see when a dynamic property takes effect and confirm a property is set as "
                    + "expected.";

            addGauge("property_value_rolling_statistical_window_in_milliseconds", propDesc,
                    () -> properties.metricsRollingStatisticalWindowInMilliseconds().get());

            addGauge("property_value_circuit_breaker_request_volume_threshold", propDesc,
                    () -> properties.circuitBreakerRequestVolumeThreshold().get());

            addGauge("property_value_circuit_breaker_sleep_window_in_milliseconds", propDesc,
                    () -> properties.circuitBreakerSleepWindowInMilliseconds().get());

            addGauge("property_value_circuit_breaker_error_threshold_percentage", propDesc,
                    () -> properties.circuitBreakerErrorThresholdPercentage().get());

            addGauge("property_value_circuit_breaker_force_open", propDesc,
                    () -> booleanToNumber(properties.circuitBreakerForceOpen().get()));

            addGauge("property_value_circuit_breaker_force_closed", propDesc,
                    () -> booleanToNumber(properties.circuitBreakerForceClosed().get()));

            addGauge("property_value_execution_timeout_in_milliseconds", propDesc,
                    () -> properties.executionTimeoutInMilliseconds().get());

            addGauge("property_value_execution_isolation_strategy", propDesc,
                    () -> properties.executionIsolationStrategy().get().ordinal());

            addGauge("property_value_metrics_rolling_percentile_enabled", propDesc,
                    () -> booleanToNumber(properties.metricsRollingPercentileEnabled().get()));

            addGauge("property_value_request_cache_enabled", propDesc,
                    () -> booleanToNumber(properties.requestCacheEnabled().get()));

            addGauge("property_value_request_log_enabled", propDesc,
                    () -> booleanToNumber(properties.requestLogEnabled().get()));

            addGauge("property_value_execution_isolation_semaphore_max_concurrent_requests", propDesc,
                    () -> properties.executionIsolationSemaphoreMaxConcurrentRequests().get());

            addGauge("property_value_fallback_isolation_semaphore_max_concurrent_requests", propDesc,
                    () -> properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get());
        }
    }

    private void createCumulativeCountForEvent(String name, final HystrixRollingNumberEvent event) {
        String doc = "These are cumulative counts since the start of the application.";
        addGauge(name, doc, () -> metrics.getCumulativeCount(event));
    }

    private void createRollingCountForEvent(String name, final HystrixRollingNumberEvent event) {
        String doc = "These are \"point in time\" counts representing the last X seconds.";
        addGauge(name, doc, () -> metrics.getRollingCount(event));
    }

    private void createExcecutionTimePercentile(String name, final double percentile, String documentation) {
        addGauge(name, documentation, () -> metrics.getExecutionTimePercentile(percentile));
    }

    private void createTotalTimePercentile(String name, final double percentile, String documentation) {
        addGauge(name, documentation, () -> metrics.getTotalTimePercentile(percentile));
    }

    private void addGauge(String metric, String helpDoc, Callable<Number> value) {
        collector.addGauge("hystrix_command", metric, helpDoc, labels, value);
    }

    private int booleanToNumber(boolean value) {
        return value ? 1 : 0;
    }
}
