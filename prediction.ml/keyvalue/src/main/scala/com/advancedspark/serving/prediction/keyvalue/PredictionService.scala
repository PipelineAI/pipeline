package com.advancedspark.serving.prediction.keyvalue

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

import io.prometheus.client.hotspot.StandardExports

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("prediction_keyvalue")

  new StandardExports().register()
  
  val namespace = ""

  val version = "" 

  val redisHostname = "redis-master"

  val redisPort = 6379
  
  val jedisPool = new JedisPool(new JedisPoolConfig(), redisHostname, redisPort);

  @RequestMapping(path=Array("/prediction/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def prediction(@PathVariable("userId") userId: String, @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemPredictionCommand("keyvalue_useritem", 25, 5, 10, -1.0d, userId, itemId)           
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  @RequestMapping(path=Array("/batch-prediction/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchPrediction(@PathVariable("userId") userId: String, @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemBatchPredictionCollapser("keyvalue_useritem_batch", 25, 5, 10, -1.0d, userId, itemId)
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  @RequestMapping(path=Array("/recommendations/{userId}/{startIdx}/{endIdx}"), 
                  produces=Array("application/json; charset=UTF-8"))
  def recommendations(@PathVariable("userId") userId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try{
      
      // TODO:  try (Jedis jedis = pool.getResource()) { }; pool.destroy();

      val results = new RecommendationsCommand(jedisPool.getResource, namespace, version, userId, startIdx, endIdx)
       .execute()
      s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/similars/{itemId}/{startIdx}/{endIdx}"),
                  produces=Array("application/json; charset=UTF-8"))
  def similars(@PathVariable("itemId") itemId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try {
       val results = new ItemSimilarsCommand(jedisPool.getResource, namespace, version, itemId, startIdx, endIdx)
         .execute()
       s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
