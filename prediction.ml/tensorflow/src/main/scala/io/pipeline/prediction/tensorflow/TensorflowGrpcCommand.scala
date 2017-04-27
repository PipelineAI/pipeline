package io.pipeline.prediction.tensorflow

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolProperties

object TensorflowGrpcCommandOps {
    val client = new com.fluxcapacitor.TensorflowPredictionClientGrpc("127.0.0.1", 9000);
}

class TensorflowGrpcCommand(commandName: String, 
                            namespace: String, 
                            modelName: String, 
                            version: Integer, 
                            inputs: Map[String, Any], 
                            fallback: String, 
                            timeout: Int, 
                            concurrencyPoolSize: Int, 
                            rejectionThreshold: Int)
  extends HystrixCommand[String](
      HystrixCommand.Setter
        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(commandName))
        .andCommandKey(HystrixCommandKey.Factory.asKey(commandName))
        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(commandName))
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
    val results = TensorflowGrpcCommandOps.client.predict(namespace, modelName, version, "")

    s"""${results}"""
  }

  override def getFallback(): String = {
    s"""${fallback}"""
  }
}
