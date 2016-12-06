package com.advancedspark.serving.prediction

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.Predictable
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import java.io.File

class TensorFlowInceptionImageClassificationCommand(image: File) 
    extends HystrixCommand[Seq[(String, Double)]](HystrixCommandGroupKey.Factory.asKey("TensorFlowInceptionImageClassificationCommand")) {

  def run(): Seq[(String, Double)] = {
    try{
      // TODO:  Call gRPC-based TensorFlow Service
      return List(("panda",10.2),("bear",9.2))
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Seq[(String, Double)] = {
    System.out.println("TensorFlow Serving for Inception Image Classifications is Down!  Fallback!!")

    List(("UNKNOWN",100))
  }
}
