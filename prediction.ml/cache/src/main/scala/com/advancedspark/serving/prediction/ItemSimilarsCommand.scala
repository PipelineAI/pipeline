package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import redis.clients.jedis._

import scala.util.parsing.json._

import collection.JavaConverters._
import scala.collection.immutable.List

class ItemSimilarsCommand(
    dynoClient: Jedis, namespace: String, version: String, itemId: String, startIdx: Int, endIdx: Int) 
  extends HystrixCommand[Seq[(String)]](HystrixCommandGroupKey.Factory.asKey("ItemSimilars")) {

  def run(): Seq[String] = {
    try{
      dynoClient.zrevrange(s"${namespace}:${version}:item-similars:${itemId}", startIdx, endIdx).asScala.toSeq
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  override def getFallback(): Seq[String] = {
    System.out.println("ItemSimilars Source is Down!  Fallback!!")

    List("10001", "10002", "10003", "10004", "10005")
  }
}
