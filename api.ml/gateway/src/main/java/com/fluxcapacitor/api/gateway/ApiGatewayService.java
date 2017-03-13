package com.fluxcapacitor.api.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import com.fluxcapacitor.api.gateway.filters.route.CanaryRoutingFilter;
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;

import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;

//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
//@EnableDiscoveryClient
//@Controller
@SpringBootApplication
@RestController
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableZuulProxy
public class ApiGatewayService {
  static {
	HystrixPrometheusMetricsPublisher.register("api_gateway");
	
	new StandardExports().register();
  }
	
  @Bean
  public CanaryRoutingFilter routeFilter() {
	return new CanaryRoutingFilter();
  }
	
  public static void main(String[] args) {
    new SpringApplicationBuilder(ApiGatewayService.class).web(true).run(args);
  }
}
