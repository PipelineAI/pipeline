package com.fluxcapacitor.api.gateway;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

//import io.prometheus.client.hotspot.StandardExports;
//import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
//import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;

//@EnablePrometheusEndpoint
//@EnableSpringBootMetricsCollector
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableDiscoveryClient
@Controller
@EnableZuulProxy
public class GatewayProxyApplication {
//    {
//      new StandardExports().register();
//    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(GatewayProxyApplication.class).web(true).run(args);
    }
}
