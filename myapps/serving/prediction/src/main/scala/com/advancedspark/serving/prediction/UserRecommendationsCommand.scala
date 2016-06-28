package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import com.netflix.dyno.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List

class UserRecommendationsCommand(userId: Int, dynoClient: DynoJedisClient) 
    extends HystrixCommand[Seq[String]](HystrixCommandGroupKey.Factory.asKey("UserRecommendationsCommand")) {

  def run(): Seq[String] = {
//    val userFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/personalized-als/_search?q=userId:${userId}""")
//    System.out.println(userFactorsStr)

    try{
      val recommendations = dynoClient.lrange(s"recommendations:${userId}", 0, 9)
      recommendations.asScala
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Seq[String] = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("UserRecommendations Source is Down!  Fallback!!")

    List("10001", "10002", "10003", "10004", "10005");
  }
}
