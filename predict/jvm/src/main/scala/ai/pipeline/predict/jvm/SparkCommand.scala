package ai.pipeline.predict.jvm

import java.io.File

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

import ml.combust.mleap.runtime.frame.FrameBuilder
import ml.combust.mleap.runtime.frame.DefaultLeapFrame
import ml.combust.mleap.runtime.transformer.Pipeline
import ml.combust.mleap.runtime.frame.Transformer


class SparkCommand(modelName: String, 
                   modelTag: String,
                   modelType: String, 
                   modelRuntime: String,
                   modelChip: String,
                   // modelTransformer: Transformer, 
                   inputs: Map[String, Any],
                   fallback: String, 
                   timeout: Int, 
                   concurrencyPoolSize: Int, 
                   rejectionThreshold: Int)
  extends HystrixCommand[String](
    HystrixCommand.Setter
      .withGroupKey(HystrixCommandGroupKey.Factory.asKey(modelName + ":" + modelTag))
      .andCommandKey(HystrixCommandKey.Factory.asKey(modelName + ":" + modelTag))
      .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(modelName + ":" + modelTag))
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
       // val s = scala.io.Source.fromFile("pipeline_test_request.json").mkString
       // val bytes = s.getBytes("UTF-8")

       // val frame = FrameReader("ml.combust.mleap.json").fromBytes(bytes)
       // val transformed_frame = mleapPipeline.transform(frame.get)
       // val data = transformed_frame.get.dataset
       // s"""${data}"""
        
       s"""
          {
            "classes": [
              7
            ],
            "probabilities": [
              [
                0.011090366169810295,
                0.000009538731319480576,
                0.00006389449117705226,
                0.0006141681806184351,
                0.0037147400435060263,
                0.005028672982007265,
                0.00008352005534106866,
                0.925011157989502,
                0.0032481630332767963,
                0.05113571882247925
              ]
            ]
          }
       """
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
