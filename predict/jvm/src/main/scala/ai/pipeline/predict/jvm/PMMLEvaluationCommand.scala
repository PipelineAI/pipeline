package ai.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.jpmml.evaluator.Evaluator

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class PMMLEvaluationCommand(modelName: String, 
                            modelTag: String,
                            modelType:String,
                            modelRuntime: String, 
                            modelChip: String,
                            modelEvaluator: Evaluator, 
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
      val inputFields = modelEvaluator.getInputFields().asScala

      val arguments =
        ( for(inputField <- inputFields)
          // The raw value is passed through:
          //   1) outlier treatment,
          //   2) missing value treatment,
          //   3) invalid value treatment
          //   4) type conversion
          yield (inputField.getName -> inputField.prepare(inputs(inputField.getName.getValue)))
        ).toMap.asJava

      val results = modelEvaluator.evaluate(arguments)
      val targetField = modelEvaluator.getTargetFields().asScala(0)
      val targetValue = results.get(targetField.getName)

      s"""{"${targetField.getName}": "${targetValue}"}"""
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
