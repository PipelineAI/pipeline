package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import com.netflix.dyno.jedis._

import scala.util.parsing.json._

class ItemSimilarsCommand(
    dynoClient: DynoJedisClient, namespace: String, version: String, itemId: String, startIdx: Int, endIdx: Int) 
  extends HystrixCommand[Seq[(String)]](HystrixCommandGroupKey.Factory.asKey("ItemSimilars")) {

  def run(): Seq[String] = {
    try{
      Seq("10001", "10002", "10003", "10004", "10005")
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

    System.out.println("ItemSimilars Source is Down!  Fallback!!")

    Seq("10001");
  }
}
