package io.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.jpmml.evaluator.Evaluator

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class HttpEvaluationCommand(modelUrl: String,
                            modelType: String,
                            modelName: String,
                            inputs: Map[String, Any], 
                            fallback: String, 
                            timeout: Int, 
                            concurrencyPoolSize: Int, 
                            rejectionThreshold: Int)
    extends HystrixCommand[String](
      HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(modelType + "_" + modelName))
        .andCommandKey(HystrixCommandKey.Factory.asKey(modelType + "_" + modelName))
        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(modelType + "_" + modelName))
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
       val results = org.apache.http.client.fluent.Request
         .Get(modelUrl)
         .execute()
         .returnContent();
                                                                                
      s"""${results}"""
    } catch { 
       case e: Throwable => {
         // System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): String = {
    // System.out.println("PMML Evaluator is Down!  Fallback!!")

    s"""${fallback}"""
  }
}
