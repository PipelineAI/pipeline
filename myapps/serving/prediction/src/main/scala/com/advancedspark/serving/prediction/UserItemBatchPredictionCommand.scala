package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

import com.netflix.dyno.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List
//
// TODO  Imlpement hystrix comllapsing/batching as follows:
//         http://www.nurkiewicz.com/2014/11/batching-collapsing-requests-in-hystrix.html
//         https://github.com/Netflix/Hystrix/wiki/How-To-Use#Collapsing
//
class UserItemBatchPredictionCommand(
      dynoClient: DynoJedisClient, namespace: String, version: String, userIds: Array[String], itemIds: Array[String])
    extends HystrixCommand[Array[Double]](HystrixCommandGroupKey.Factory.asKey("UserItemBatchPrediction")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Array[Double] = {
    try{
      val userFactorsArray = userIds.map(userId => 
        dynoClient.get(s"${namespace}:${version}:user-factors:${userId}").split(",").map(_.toDouble)
      )
      val itemFactorsArray = itemIds.map(itemId =>
        dynoClient.get(s"${namespace}:${version}:item-factors:${itemId}").split(",").map(_.toDouble)
      ) 

      val userFactorsMatrix = new DoubleMatrix(userFactorsArray)
      val itemFactorsMatrix = new DoubleMatrix(itemFactorsArray)
     
      // Calculate prediction 
      userFactorsMatrix.mmul(itemFactorsMatrix).data
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Array[Double] = {
    System.out.println("UserItemBatchPrediction Source is Down!  Fallback!!")

    Array(0.0)
  }
}
