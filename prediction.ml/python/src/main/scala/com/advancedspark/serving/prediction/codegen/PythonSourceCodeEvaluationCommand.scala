package com.advancedspark.serving.prediction.python

import scala.collection.JavaConversions._
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolProperties
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class PythonSourceCodeEvaluationCommand(name: String, filename: String, inputs: Map[String, Any],
    fallback: String, timeout: Int, concurrencyPoolSize: Int, rejectionThreshold: Int)
  extends HystrixCommand[String](
    HystrixCommand.Setter
      .withGroupKey(HystrixCommandGroupKey.Factory.asKey(name))
      .andCommandKey(HystrixCommandKey.Factory.asKey(name))
      .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(name))
      .andCommandPropertiesDefaults(
        HystrixCommandProperties.Setter()
         .withExecutionTimeoutInMilliseconds(timeout)
         .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
         .withExecutionIsolationSemaphoreMaxConcurrentRequests(concurrencyPoolSize)
         .withFallbackIsolationSemaphoreMaxConcurrentRequests(rejectionThreshold)
      )
      .andThreadPoolPropertiesDefaults(
        HystrixThreadPoolProperties.Setter()
          .withCoreSize(concurrencyPoolSize)
          .withQueueSizeRejectionThreshold(rejectionThreshold)
      )
    )
{
  def run(): String = {
    try{
      val p = Runtime.getRuntime().exec(s"python ${filename}")

      val stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

      val stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      // read the output from the command
      s"Success ${stdInput.lines().collect(Collectors.joining("\n"))}, Error ${stdError.lines().collect(Collectors.joining("\n"))}"       
    } catch { 
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  override def getFallback(): String = {
    s"""${fallback}"""
  }
}