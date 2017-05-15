package io.pipeline.prediction.jvm

import java.nio.file.Paths

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class SparkEvaluationCommand(commandName: String, 
                             namespace: String, 
                             modelName: String, 
                             version: String, 
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
  val modelDir = s"/root/model_store/spark/${namespace}/${modelName}/${version}"

  val model = FileUtil.readAllBytesOrExit(Paths.get(modelDir, "model.parquet"))

  def run(): String = {
    try{
      val k = 5
      val results = new Array[String](k)
/*      
      val image = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(
        Paths.get(s"/root/model_store/.../images/${inputName}")))      

      val labelProbabilities = LabelImage.executeInceptionGraph(graphDef, image)

      val bestLabelIdxs = LabelImage.maxKIndex(labelProbabilities, k)
      
      for (i <- 0 until k) {
        results(i) =
          s"""{'${labels.get(bestLabelIdxs(i))}':${labelProbabilities(bestLabelIdxs(i)) * 100f}}"""        
      }
*/
      s"""${results.mkString(",")}"""
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
