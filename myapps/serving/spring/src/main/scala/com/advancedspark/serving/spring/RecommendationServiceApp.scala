package com.advancedspark.serving.spring

import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.stereotype._
import org.springframework.web.bind.annotation._
import org.springframework.boot.context.embedded._
import org.springframework.context.annotation._

import scala.collection.JavaConversions._

import com.netflix.hystrix.contrib.requestservlet.HystrixRequestContextServletFilter
import com.netflix.hystrix.contrib.requestservlet.HystrixRequestLogViaResponseHeaderServletFilter
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import com.netflix.hystrix.contrib.sample.stream.HystrixConfigSseServlet
import com.netflix.hystrix.contrib.sample.stream.HystrixUtilizationSseServlet
import com.netflix.hystrix.contrib.requests.stream.HystrixRequestEventsSseServlet

@Configuration
@RestController
@EnableAutoConfiguration
class RecommendationService {
  @RequestMapping(Array("/prediction/{userId}/{itemId}"))
  def predict(@PathVariable("userId") userId: Int, @PathVariable("itemId") itemId: Int): String = {
    val prediction = new UserItemPredictionCommand(userId, itemId).execute()
    "userId:" + userId + ", itemId:" + itemId + ", prediction:" + prediction
  }

  @RequestMapping(Array("/recommendations/{userId}"))
  def recommendations(@PathVariable("userId") userId: Int): String = {
    val recommendations = new UserRecommendationsCommand(userId).execute()
    "userId:" + userId + ", recommendations:" + recommendations 
  }

  @RequestMapping(Array("/similars/{itemId}"))
  def similars(@PathVariable("itemId") itemId: Int): String = {
    val similars = new ItemSimilarsCommand(itemId).execute()
    "itemId:" + itemId + ", similars:" + similars
  }

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
}

object RecommendationServiceApp {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[RecommendationService])
  }
}
