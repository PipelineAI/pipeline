package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

import redis.clients.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List

class UserItemPredictionCommand(
      jedis: Jedis, namespace: String, version: String, userId: String, itemId: String)
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("UserItemPrediction")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

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
