/*
 * Copyright (C) 2016 Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.fabric8.kubeflix;

import com.netflix.turbine.discovery.InstanceDiscovery;
import com.netflix.turbine.streaming.servlet.TurbineStreamServlet;
import io.fabric8.kubeflix.turbine.TurbineDiscovery;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.configuration.MapConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(AggregatorProperties.class)
public class TurbineConfiguration {

    private static final String DEFAULT_TURBINE_URL_MAPPING = "/turbine.stream";
    private static final String DEFAULT_DISCOVERY_URL_MAPPING = "/discovery";
    @Autowired
    AggregatorProperties aggregatorProperties;

    @Bean
    InstanceDiscovery instanceDiscovery(KubernetesClient client) {
        return new TurbineDiscovery(client, aggregatorProperties.getClusters());
    }

    @Bean
    TurbineLifecycle turbineContextListener(InstanceDiscovery instanceDiscovery) {
        return new TurbineLifecycle(instanceDiscovery);
    }

    @Bean
    public ServletRegistrationBean turbineServletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(turbineStreamServlet());
        registration.setUrlMappings(Arrays.asList(DEFAULT_TURBINE_URL_MAPPING));
        return registration;
    }

    @Bean
    public ServletRegistrationBean discoveryServletRegistration(InstanceDiscovery instanceDiscovery) {
        ServletRegistrationBean registration = new ServletRegistrationBean(discoveryFeedbackServlet(instanceDiscovery));
        registration.setUrlMappings(Arrays.asList(DEFAULT_DISCOVERY_URL_MAPPING));
        return registration;
    }

    @Bean
    public TurbineStreamServlet turbineStreamServlet() {
        return new TurbineStreamServlet();
    }

    @Bean
    public DiscoveryFeedbackServlet discoveryFeedbackServlet(InstanceDiscovery instanceDiscovery) {
        return new DiscoveryFeedbackServlet(instanceDiscovery);
    }
}
