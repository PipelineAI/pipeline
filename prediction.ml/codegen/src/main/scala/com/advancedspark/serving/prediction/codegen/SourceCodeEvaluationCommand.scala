package com.advancedspark.serving.prediction.codegen

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixThreadPoolKey

class SourceCodeEvaluationCommand(name: String, predictor: Predictable, inputs: Map[String,Any]) 
    extends HystrixCommand[String](HystrixCommand.Setter
      .withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
      .andCommandKey(HystrixCommandKey.Factory.asKey(name))
      .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(name))
  ) {

  def run(): String = {
    try{
      s"""${predictor.predict(inputs)}"""
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
