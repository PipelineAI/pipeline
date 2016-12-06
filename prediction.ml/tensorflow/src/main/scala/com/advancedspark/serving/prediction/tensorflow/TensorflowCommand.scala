package com.advancedspark.serving.prediction.tensorflow

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixThreadPoolKey

class TensorflowCommand(name: String, model: Array[Byte], inputs: Map[String, Any])
    extends HystrixCommand[String](HystrixCommand.Setter
      .withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
      .andCommandKey(HystrixCommandKey.Factory.asKey(name))
      .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(name))
  ) {

  def run(): String = {
    try{
      s"""[]"""
    } catch { 
       case e: Throwable => {
         throw e
       }
    }
  }

  override def getFallback(): String = {
    s"""[]"""
  }
}
