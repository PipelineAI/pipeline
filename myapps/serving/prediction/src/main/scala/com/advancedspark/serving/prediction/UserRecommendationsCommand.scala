package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._
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

//import scala.collection.JavaConverters._

import java.util.Collections
import java.util.Collection
import java.util.Set
import java.util.List

class UserRecommendationsCommand(userId: Int) 
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("UserRecommendationsCommand")) {

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

//  @throws(classOf[java.io.IOException])
//  def get(url: String) = io.Source.fromURL(url).mkString

  def run(): Double = {
//    val userFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/personalized-als/_search?q=userId:${userId}""")
//    System.out.println(userFactorsStr)

    try{
      val retrievedRecommendations = dynoClient.get("personalized-als")
      1.0
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Double = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("UserRecommendations Source is Down!  Fallback!!")

    0.0;
  }
}
