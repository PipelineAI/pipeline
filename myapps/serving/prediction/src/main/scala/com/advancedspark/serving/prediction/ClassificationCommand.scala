package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

class ClassificationCommand(itemId: Int) 
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("ClassificationCommand")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = io.Source.fromURL(url).mkString

  def run(): Double = {
    try{
      System.out.println("TODO")
      1.0 
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

    System.out.println("Classification Source is Down!  Fallback!!")

    0.0;
  }
}
