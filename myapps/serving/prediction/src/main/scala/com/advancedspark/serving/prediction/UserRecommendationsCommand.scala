package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import com.netflix.dyno.jedis._

class UserRecommendationsCommand(userId: Int, dynoClient: DynoJedisClient) 
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("UserRecommendationsCommand")) {

  def run(): Double = {
//    val userFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/personalized-als/_search?q=userId:${userId}""")
//    System.out.println(userFactorsStr)

    try{
      val recommendations = dynoClient.get("recommendations")
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
