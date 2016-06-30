package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

import com.netflix.dyno.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List

class UserItemPredictionCommand(dynoClient: DynoJedisClient, version: Int, userId: Int, itemId: Int)
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("UserItemPredictionCommand")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Double = {
    try{
      val userFactors = dynoClient.get(s"user-factors:${userId}").split(",").map(_.toDouble)
      val itemFactors = dynoClient.get(s"item-factors:${itemId}").split(",").map(_.toDouble)

      val userFactorsMatrix = new DoubleMatrix(userFactors)
      val itemFactorsMatrix = new DoubleMatrix(itemFactors)
     
      // Calculate prediction 
      userFactorsMatrix.dot(itemFactorsMatrix)
//      1.1
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

    System.out.println("UserItemPrediction Source is Down!  Fallback!!")

    0.0;
  }
}
