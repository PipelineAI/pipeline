package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import redis.clients.jedis._

import scala.util.parsing.json._

class ImageClassificationCommand(dynoClient: Jedis, namespace: String, version: String, url: String) 
    extends HystrixCommand[Seq[(String, Double)]](HystrixCommandGroupKey.Factory.asKey("ImageClassification")) {

  //@throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Seq[(String, Double)] = {
    try{
      //http://<ip>:5070/classify/cropped_panda.jpg

      Seq(("Panda", 100.0))

    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Seq[(String, Double)] = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("Image Classification Source is Down!  Fallback!!")

    Seq(("Unknown", 100.0))
  }
}
