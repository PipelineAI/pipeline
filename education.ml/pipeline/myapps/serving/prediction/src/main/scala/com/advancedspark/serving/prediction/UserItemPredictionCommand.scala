package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

import com.netflix.dyno.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List

class UserItemPredictionCommand(
      dynoClient: DynoJedisClient, namespace: String, version: String, userId: String, itemId: String)
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("UserItemPrediction")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Double = {
    try{
      val userFactors = dynoClient.get(s"${namespace}:${version}:user-factors:${userId}").split(",").map(_.toDouble)
      val itemFactors = dynoClient.get(s"${namespace}:${version}:item-factors:${itemId}").split(",").map(_.toDouble)

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
