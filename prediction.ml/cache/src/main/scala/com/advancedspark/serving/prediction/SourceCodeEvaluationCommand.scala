package com.advancedspark.serving.prediction

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.Predictable
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

class SourceCodeEvaluationCommand(predictor: Predictable, key: String) 
    extends HystrixCommand[String](HystrixCommandGroupKey.Factory.asKey("SourceCodeEvaluationCommand")) {

  def run(): String = {
    try{
      s"""${predictor.predict(key)}"""
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): String = {
    System.out.println("SourceCode Evaluator is Down!  Fallback!!")

    "UNKNOWN"
  }
}
