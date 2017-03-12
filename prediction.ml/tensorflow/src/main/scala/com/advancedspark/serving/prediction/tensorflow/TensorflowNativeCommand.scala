package com.advancedspark.serving.prediction.tensorflow

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties
import org.tensorflow.Tensor
import java.util.ArrayList

import java.nio.file.Paths

class TensorflowNativeCommand(name: String, inputs: Map[String, Any],
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
  val modelDir = "/root/store/tensorflow_inception/export/00000001"

  val imageFile = "/root/store/images/advanced-spark-and-tensorflow-meetup-6000-cake.jpg"

  val graphDef = LabelImage.readAllBytesOrExit(Paths.get(modelDir, "tensorflow_inception_graph.pb"))

  val labels = LabelImage.readAllLinesOrExit(Paths.get(modelDir, "imagenet_comp_graph_label_strings.txt"))

  val imageBytes = LabelImage.readAllBytesOrExit(Paths.get(imageFile))

  val image: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes)

  val k = 10

  def run(): String = {
    try{
      val results = new java.util.ArrayList[String](k)
      
      val labelProbabilities = LabelImage.executeInceptionGraph(graphDef, image)

      val bestLabelIdxs = LabelImage.maxKIndex(labelProbabilities, k)
      
      for (i <- 0 until k) {
        results.add(
          s"""BEST MATCH: ${labels.get(bestLabelIdxs(i))} ${labelProbabilities(bestLabelIdxs(i)) * 100f}% likely)"""
        )
      }

      System.out.println(results)

      s"""${results}"""
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