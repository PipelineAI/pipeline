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

class TensorflowNativeCommand(commandName: String, 
                              namespace: String, 
                              modelName: String, 
                              version: Integer, 
                              inputs: Map[String, Any],
    fallback: String, timeout: Int, concurrencyPoolSize: Int, rejectionThreshold: Int)
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
/*
  val image0: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/0.jpg")))
  val image1: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/1.jpg")))
  val image2: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/2.jpg")))
  val image3: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/3.jpg")))
  val image4: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/4.jpg")))
  val image5: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/5.jpg")))
  val image6: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/6.jpg")))
  val image7: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/7.jpg")))
  val image8: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/8.jpg")))
  val image9: Tensor = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(Paths.get("/root/store/images/9.jpg")))

  val images = Array(image0, image1, image2, image3, image4, image5, image6, image7, image8, image9)
*/
  val k = 10
  val randomInt = scala.util.Random

  def run(): String = {
    try{
      val results = new Array[String](k)

      // val image = images(randomInt.nextInt(10)) 

      val image = LabelImage.constructAndExecuteGraphToNormalizeImage(LabelImage.readAllBytesOrExit(
        Paths.get(s"/root/store/${namespace}/images/${version}/${randomInt.nextInt(10)}.jpg")))

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
