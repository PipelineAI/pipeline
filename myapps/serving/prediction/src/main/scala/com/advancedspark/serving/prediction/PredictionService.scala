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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.netflix.dyno.jedis._
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.connectionpool.exception.DynoException
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.dyno.connectionpool.impl.ConnectionContextImpl
import com.netflix.dyno.connectionpool.impl.OperationResultImpl
import com.netflix.dyno.connectionpool.impl.utils.ZipUtils

@SpringBootApplication
@RestController
@EnableHystrix
@EnableEurekaClient
class PredictionService {
  @Bean
//  @RefreshScope
  val version = 0

  @RequestMapping(Array("/prediction/{userId}/{itemId}"))
  def prediction(@PathVariable("userId") userId: Int, @PathVariable("itemId") itemId: Int): String = {
    val pred = getPrediction(userId, itemId)

    "userId:" + userId + ", itemId:" + itemId + ", prediction:" + pred
  }

  def getPrediction(userId: Int, itemId: Int): Double = {
    val pred = new UserItemPredictionCommand(PredictionServiceOps.dynoClient, version, userId, itemId).execute()
    pred
    //1.0
  }

  @RequestMapping(Array("/recommendations/{userId}"))
  def recommendations(@PathVariable("userId") userId: Int): String = {
    val recs = getRecommendations(userId) 
    "userId:" + userId + ", recommendations:" + recs
  }


  def getRecommendations(userId: Int): String = {
    try{
      val recommendations = new UserRecommendationsCommand(PredictionServiceOps.dynoClient, version, userId).execute()
      recommendations.toString
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(Array("/similars/{itemId}"))
  def similars(@PathVariable("itemId") itemId: Int): String = {
    // TODO:  
    val sims = "1.0"
    "itemId:" + itemId + ", similars:" + sims
  }

  @RequestMapping(Array("/classifications/{itemId}"))
  def classifications(@PathVariable("itemId") itemId: Int): String = {
    // TODO:
    val classes = "1.0"
    "itemId:" + itemId + ", classifications: " + classes
  }
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}

object PredictionServiceOps {
  val localhostHost = new Host("demo.pipeline.io", Host.Status.Up)
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

