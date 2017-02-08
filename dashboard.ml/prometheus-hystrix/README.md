# Derived from https://github.com/soundcloud/prometheus-hystrix

# prometheus-hystrix-metrics-publisher

This is an implementation of a [HystrixMetricsPublisher](http://netflix.github.com/Hystrix/javadoc/index.html?com/netflix/hystrix/strategy/metrics/HystrixMetricsPublisher.html)
that publishes metrics using Prometheus' [SimpleClient](https://github.com/prometheus/client_java).

See the [Netflix Metrics & Monitoring](https://github.com/Netflix/Hystrix/wiki/Metrics-and-Monitoring) Wiki for more information.

## USAGE

Register the metrics publisher for your application's namespace and the default Prometheus CollectorRegistry with Hystrix.

```java
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;

// ...

HystrixPrometheusMetricsPublisher.register("application_name");
```

Register the publisher for your application's namespace with your own Prometheus CollectorRegistry with Hystrix.

```java
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;
import io.prometheus.client.CollectorRegistry;

// ...

CollectorRegistry registry = // ...
HystrixPrometheusMetricsPublisher.register("application_name", registry);
```

## DEVELOPMENT

Run `./gradlew` to compile, test and create POM & JARs.

Run `./gradlew -PreleaseBuild=true -PbuildVersion=1.0.0 clean releaseJar` to create a distributable bundle
ready for upload to Sonatype OSS repository. Before you run this make sure you have permissions to upload
and that your GPG keys are configured.

## LICENSE

Copyright 2014 SoundCloud, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
