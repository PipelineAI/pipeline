package com.advancedspark.serving.prediction

import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.stereotype._
import org.springframework.web.bind.annotation._
import org.springframework.boot.context.embedded._
import org.springframework.context.annotation._

import scala.collection.JavaConversions._
import java.util.Collections
import java.util.Collection
import java.util.Set
import java.util.List

import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
//import com.netflix.hystrix.contrib.requestservlet.HystrixRequestContextServletFilter
//import com.netflix.hystrix.contrib.requestservlet.HystrixRequestLogViaResponseHeaderServletFilter
//import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
//import com.netflix.hystrix.contrib.sample.stream.HystrixConfigSseServlet
//import com.netflix.hystrix.contrib.sample.stream.HystrixUtilizationSseServlet
//import com.netflix.hystrix.contrib.requests.stream.HystrixRequestEventsSseServlet

import com.netflix.dyno.jedis._
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.connectionpool.exception.DynoException
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.dyno.connectionpool.impl.ConnectionContextImpl
import com.netflix.dyno.connectionpool.impl.OperationResultImpl
import com.netflix.dyno.connectionpool.impl.utils.ZipUtils

@Configuration
@RestController
@EnableAutoConfiguration
@EnableHystrix
@EnableHystrixDashboard
@EnableEurekaClient
class PredictionService {
  // TODO:  Fold the Command code into the methods here wrapped with @HystrixCommand
  // Follow this to give human-readable names:  
  //    https://github.com/Netflix/Hystrix/blob/master/hystrix-contrib/hystrix-javanica/src/main/java/com/netflix/hystrix/contrib/javanica/annotation/HystrixCommand.java
//  @HystrixCommand
  @RequestMapping(Array("/prediction/{userId}/{itemId}"))
  def predict(@PathVariable("userId") userId: Int, @PathVariable("itemId") itemId: Int): String = {
    val prediction = new UserItemPredictionCommand(userId, itemId).execute()
    "userId:" + userId + ", itemId:" + itemId + ", prediction:" + prediction
  }

  @HystrixCommand(groupKey="PredictionService", commandKey="recommendations", threadPoolKey="recommendations")
  @RequestMapping(Array("/recommendations/{userId}"))
  def recommendations(@PathVariable("userId") userId: Int): String = {
//    val recommendations = new UserRecommendationsCommand(userId).execute()
    val recommendations = PredictionServiceOps.dynoClient.get("personalized-als")
    "userId:" + userId + ", recommendations:" + recommendations(0) 
  }

//  @HystrixCommand
  @RequestMapping(Array("/similars/{itemId}"))
  def similars(@PathVariable("itemId") itemId: Int): String = {
    val similars = new ItemSimilarsCommand(itemId).execute()
    "itemId:" + itemId + ", similars:" + similars
  }

//  @HystrixCommand
  @RequestMapping(Array("/classification/{itemId}"))
  def classify(@PathVariable("itemId") itemId: Int): String = {
    val classification = new ClassificationCommand(itemId).execute()
    "itemId:" + itemId + ", classification: " + classification
  }
/**
  @Bean
  def hystrixRequestContextServletFilter(): FilterRegistrationBean = {
    val registrationBean = new FilterRegistrationBean()
    val filter = new HystrixRequestContextServletFilter()
    registrationBean.setFilter(filter)
    registrationBean
  }

  @Bean
  def hystrixRequestLogViaResponseHeaderServletFilter(): FilterRegistrationBean = {
    val registrationBean = new FilterRegistrationBean()
    val filter = new HystrixRequestLogViaResponseHeaderServletFilter()
    registrationBean.setFilter(filter)
    registrationBean
  }

  @Bean
  def hystrixMetricsStreamServlet(): ServletRegistrationBean = {
    val registrationBean = new ServletRegistrationBean()
    val servlet = new HystrixMetricsStreamServlet()
    registrationBean.setServlet(servlet)
    registrationBean.setUrlMappings(Seq("/hystrix.stream"))
    registrationBean
  }

  @Bean
  def hystrixConfigSseServlet(): ServletRegistrationBean = {
    val registrationBean = new ServletRegistrationBean()
    val servlet = new HystrixConfigSseServlet()
    registrationBean.setServlet(servlet)
    registrationBean.setUrlMappings(Seq("/hystrix/config.stream"))
    registrationBean
  }

  @Bean
  def hystrixUtilizationSseServlet(): ServletRegistrationBean = {
    val registrationBean = new ServletRegistrationBean()
    val servlet = new HystrixUtilizationSseServlet()
    registrationBean.setServlet(servlet)
    registrationBean.setUrlMappings(Seq("/hystrix/utilization.stream"))
    registrationBean
  }

  @Bean
  def hystrixRequestEventsSseServlet(): ServletRegistrationBean = {
    val registrationBean = new ServletRegistrationBean()
    val servlet = new HystrixRequestEventsSseServlet()
    registrationBean.setServlet(servlet)
    registrationBean.setUrlMappings(Seq("/hystrix/requests.stream"))
    registrationBean
  }
*/
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}

object PredictionServiceOps {
  val localhostHost = new Host("127.0.0.1", Host.Status.Up)
  val localhostToken = new HostToken(100000L, localhostHost)

  val localhostHostSupplier = new HostSupplier() {
    @Override
    def getHosts(): Collection[Host] = {
      Collections.singletonList(localhostHost)
    }
  }

  val localhostTokenMapSupplier = new TokenMapSupplier() {
    @Override
    def getTokens(activeHosts: Set[Host]): List[HostToken] = {
      Collections.singletonList(localhostToken)
    }

    @Override
    def getTokenForHost(host: Host, activeHosts: Set[Host]): HostToken = {
      return localhostToken
    }
  }

  val redisPort = 6379
  val dynoClient = new DynoJedisClient.Builder()
             .withApplicationName("pipeline")
             .withDynomiteClusterName("pipeline-dynomite")
             .withHostSupplier(localhostHostSupplier)
             .withCPConfig(new ConnectionPoolConfigurationImpl("localhostTokenMapSupplier")
                .withTokenSupplier(localhostTokenMapSupplier))
             .withPort(redisPort)
             .build()
}

