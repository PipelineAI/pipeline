package com.advancedspark.serving.recommendations

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix
//import com.advancedspark.ml.BLAS
//import com.advancedspark.ml.Vectors

import better.files.ThreadBackedFileMonitor
import better.files.File
import java.nio.file.WatchEvent

import scala.util.parsing.json._

class WatchedJsonPredictionCommand(userId: Int, itemId: Int) 
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("WatchedJsonPredictionCommand")) {

  new ThreadBackedFileMonitor(File("/root/pipeline/datasets/serving/live-recommendations/json"), recursive = true) {
    override def onCreate(file: File) = {
      println(s"New file:  $file")
      val jsonStr = new String(file.byteArray)
      println(s"Contents:  $jsonStr")

      val result = JSON.parseFull(jsonStr)
      result match {
  	case Some(map: Map[String, Any]) => println(map)
  	case None => println("Parsing failed")
  	case other => println("Unknown data structure: " + other) 
      }
    }
    override def onModify(file: File) = println(s"Modified file:  $file")
    override def onDelete(file: File) = println(s"Deleted file:  $file")
    override def onUnknownEvent(event: WatchEvent[_]) = println(event)
    override def onException(exception: Throwable) = println(exception)
  }.start()
  
  @throws(classOf[java.io.IOException])
  def get(url: String) = io.Source.fromURL(url).mkString

  def run(): Double = {
    val userFactorsStr = get(s"""file:///root/pipeline/datasets/serving/live-recommendations/json/als/itemId=${userId}/part-""")

    // This is the worst piece of code I've ever written
    val userFactorsJson = JSON.parseFull(userFactorsStr)
    val userFactors = userFactorsJson.get
      .asInstanceOf[Map[String,Any]]("hits")
      .asInstanceOf[Map[String,Any]]("hits")
      .asInstanceOf[List[Any]](0)
      .asInstanceOf[Map[String,Any]]("_source")
      .asInstanceOf[Map[String,Any]]("userFactors")
      .asInstanceOf[List[Double]]

    // This is the second worst piece of code I've ever written
    val itemFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/item-factors-als/_search?q=itemId:${itemId}""")   
    val itemFactorsJson = JSON.parseFull(itemFactorsStr)
    val itemFactors = itemFactorsJson.get
      .asInstanceOf[Map[String,Any]]("hits")
      .asInstanceOf[Map[String,Any]]("hits")
      .asInstanceOf[List[Any]](0)
      .asInstanceOf[Map[String,Any]]("_source")
      .asInstanceOf[Map[String,Any]]("itemFactors")
      .asInstanceOf[List[Double]]
 
    try{
      val userFactorsMatrix = new DoubleMatrix(userFactors.toArray)
      val itemFactorsMatrix = new DoubleMatrix(itemFactors.toArray)
      //val userFactorsVector = Vectors.dense(userFactors.toArray)
      //val itemFactorsVector = Vectors.dense(itemFactors.toArray)
     
      // Calculate confidence
      userFactorsMatrix.dot(itemFactorsMatrix)
      //userFactorsVector.dot(itemFactorsVector)
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }

   // 1.0;
  }

  override def getFallback(): Double = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("ElasticSearch is Down!  Fallback!!")

    0.0;
  }
}
