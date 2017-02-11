package com.advancedspark.serving.prediction.keyvalue

import org.jblas.DoubleMatrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import redis.clients.jedis.Jedis

class UserItemPredictionCommand(
      jedis: Jedis, namespace: String, version: String, userId: String, itemId: String)
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("recommendation_user_item_prediction")) {

  def run(): Double = {
    try{
      val userFactors = jedis.get(s"${namespace}:${version}:user-factors:${userId}").split(",").map(_.toDouble)
      val itemFactors = jedis.get(s"${namespace}:${version}:item-factors:${itemId}").split(",").map(_.toDouble)

      val userFactorsMatrix = new DoubleMatrix(userFactors)
      val itemFactorsMatrix = new DoubleMatrix(itemFactors)
     
      // Calculate prediction 
      userFactorsMatrix.dot(itemFactorsMatrix)
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Double = {
    System.out.println("UserItemPrediction Source is Down!  Fallback!!")

    0.0
  }
}
