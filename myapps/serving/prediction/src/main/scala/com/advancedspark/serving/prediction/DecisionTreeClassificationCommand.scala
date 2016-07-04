package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import com.netflix.dyno.jedis._

import scala.util.parsing.json._

// Return Map[classId (String), probability (Double)]
class DecisionTreeClassificationCommand(servableRootPath: String, version: Int, itemId: String) 
    extends HystrixCommand[Map[Int, Double]](HystrixCommandGroupKey.Factory.asKey("DecisionTreeClassification")) {

  //@throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Map[String, Double] = {
    try{
      //http://<ip>:5070/classify/cropped_panda.jpg

      System.out.println("TODO")
      Map("Panda" -> 100.0)
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Map[String, Double] = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("Decision Tree Classification Source is Down!  Fallback!!")

    Map("Unknown" -> 100.0)
  }
}
