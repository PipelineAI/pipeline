package ai.pipeline.predict.jvm

import org.jblas.DoubleMatrix

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class UserItemPredictionCommand(runtimeType: String,
                                commandName: String, 
                                namespace: String, 
                                version: String, 
                                fallback: Double, 
                                timeout: Int, 
                                concurrencyPoolSize: Int, 
                                rejectionThreshold: Int, 
                                userId: String, itemId: String)
  extends HystrixCommand[Double](
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
    ) {
  
  def run(): Double = {
    try{
//      val userFactors = jedis.get(s"::user-factors:${userId}").split(",").map(_.toDouble)
//      val itemFactors = jedis.get(s"::item-factors:${itemId}").split(",").map(_.toDouble)

      val userFactors = Array(0.99d)
      val itemFactors = Array(0.90d)
      
      val userFactorsMatrix = new DoubleMatrix(userFactors)
      val itemFactorsMatrix = new DoubleMatrix(itemFactors)
     
      // Calculate prediction 
      userFactorsMatrix.dot(itemFactorsMatrix)
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Double = {
    fallback
  }
}
