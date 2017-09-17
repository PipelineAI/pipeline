package io.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.apache.http.entity.ContentType

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class HttpEvaluationCommand(modelUrl: String,
                            modelType: String,
                            modelName: String,
                            modelTag: String,
                            requestMethod: String,
                            requestBody: String,
                            fallback: String,
                            timeout: Int,
                            concurrencyPoolSize: Int,
                            rejectionThreshold: Int)
    extends HystrixCommand[String](
      HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(modelType + "_" + modelName + "_" + modelTag))
        .andCommandKey(HystrixCommandKey.Factory.asKey(modelType + "_" + modelName + "_" + modelTag))
        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(modelType + "_" + modelName + "_" + modelTag))
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
       // TODO:  Don't hard code RequestMethod/Post and ContentType/Json
       val results = org.apache.http.client.fluent.Request
         .Post(modelUrl)
         .bodyString(requestBody, ContentType.APPLICATION_JSON)
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
    s"""${fallback}"""
  }
}
