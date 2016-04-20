package com.advancedspark.serving.recommendations

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

class PredictionCommand(userId: Int, itemId: Int) 
    extends HystrixCommand[Double](HystrixCommandGroupKey.Factory.asKey("Prediction")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = io.Source.fromURL(url).mkString

  def run(): Double = {
    val userFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/user-factors-als/_search?q=userId:${userId}""")
    val itemFactorsStr = get(s"""http://127.0.0.1:9200/advancedspark/item-factors-als/_search?q=itemId:${itemId}""")

    System.out.println(userFactorsStr)

    val userFactorsJson = JSON.parseFull(userFactorsStr)
    System.out.println(userFactorsJson)

    val userFactors = userFactorsJson
/*    
    userFactors match { 
      case Some(hits: Map[String, Any]) => hits("hits") match {
        case Some(hits: Map[String, Any]) => hits(0) match {
          case Some(source: Map[String, Any]) => source("_source") match {
            case Some(userFeatures: Map[String, Any]) => source("userFeatures") match {
              case userFactors: List[Double] => userFactors
 	    }
          }
        }
      }
    }
*/
//["hits"]['hits'][0]['_source']['userFeatures']
//    userFactorsStr.split(",")(1).toDouble

    System.out.println(userFactors)

    1.0;
  }

  override def getFallback(): Double = {
    // Retrieve fallback (ie. non-personalized top k)
    //val source = scala.io.Source.fromFile("/root/pipeline/datasets/serving/recommendations/fallback/model.json")
    //val fallbackRecommendationsModel = try source.mkString finally source.close()
    //return fallbackRecommendationsModel;

    System.out.println("Prediction Service is Down!  Fallback!!")

    0.0;
  }
}
