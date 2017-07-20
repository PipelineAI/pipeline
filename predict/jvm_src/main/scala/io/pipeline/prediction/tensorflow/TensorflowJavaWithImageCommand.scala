package io.pipeline.prediction.tensorflow

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties
import org.tensorflow.Tensor
import java.util.ArrayList

import java.nio.file.Paths

class TensorflowJavaWithImageCommand(commandName: String, 
                                     namespace: String, 
                                     modelName: String, 
                                     version: Integer, 
                                     imageName: String, 
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
  val modelDir = s"/root/store/${namespace}/${modelName}/export/${version}"

  val graphDef = LabelImage.readAllBytesOrExit(Paths.get(modelDir, "tensorflow_inception_graph.pb"))
  val labels = LabelImage.readAllLinesOrExit(Paths.get(modelDir, "imagenet_comp_graph_label_strings.txt"))

  val k = 10
  val randomInt = scala.util.Random

  def run(): String = {
    try{
      val results = new Array[String](k)
      
      val image = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(
        Paths.get(s"/root/store/${namespace}/images/${version}/${imageName}")))      

      val labelProbabilities = LabelImage.executeInceptionGraph(graphDef, image)

      val bestLabelIdxs = LabelImage.maxKIndex(labelProbabilities, k)
      
      for (i <- 0 until k) {
        results(i) =
          s"""{'${labels.get(bestLabelIdxs(i))}':${labelProbabilities(bestLabelIdxs(i)) * 100f}}"""        
      }

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
