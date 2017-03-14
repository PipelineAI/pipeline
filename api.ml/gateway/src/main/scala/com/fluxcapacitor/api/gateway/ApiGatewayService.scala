package com.fluxcapacitor.api.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation._
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.context.annotation._
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation._
import scala.util.{Try,Success,Failure}
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import java.util.stream.Collectors
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import io.prometheus.client.hotspot.StandardExports

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RestController

import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import com.fluxcapacitor.api.gateway.filters.CanaryFilter
import com.fluxcapacitor.api.gateway.filters.BanditFilter
import org.springframework.cloud.netflix.zuul.filters.route.SendForwardFilter

@SpringBootApplication
@RestController
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableZuulProxy
class ApiGatewayService {
  HystrixPrometheusMetricsPublisher.register("api_gateway")

  new StandardExports().register()

  @Bean
  def canaryFilter(): CanaryFilter = {
    new CanaryFilter();
  }
  
//  @Bean
//  def banditFilter(): BanditFilter = {
//    new BanditFilter();
//  }
//  
//  @Bean
//  def forwardFilter(): SendForwardFilter = {
//    new SendForwardFilter();
//  }
}

object ApiGatewayServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[ApiGatewayService])
  }
}
