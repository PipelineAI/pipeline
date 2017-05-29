package io.pipeline.prediction.tensorflow

import java.nio.file.Files
import java.nio.file.Paths

import org.tensorflow.Graph
import org.tensorflow.Session
import org.tensorflow.Tensor

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class TensorflowNativeCommand(commandName: String,       
                              session: Session,
                              input: Tensor,
                              inputs: Map[String, Any],
                              fallback: Float, 
                              timeout: Int, 
                              concurrencyPoolSize: Int, 
                              rejectionThreshold: Int)
  extends HystrixCommand[Tensor](
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
  val k = 10
  val randomInt = scala.util.Random

  def run(): Tensor = {
    try{
      val results = new Array[String](k)

      // TODO. session.run(feed).get(fetch)
      
      Tensor.create(0.0f)
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
      }
    }
  }

  override def getFallback(): Tensor = {
    Tensor.create(fallback)
  }
}

object TensorflowJavaCommand {
  def main(args: Array[String]): Unit = {
    val modelType = "tensorflow"
    val modelNamespace = "default"
    val modelName = "tensorflow_linear"
    val modelVersion = "0"

    //val modelParentPath = s"/root/model_store/${modelType}/${modelNamespace}/${modelName}/${modelVersion}"
    val modelParentPath = s"/Users/cfregly/workspace-fluxcapacitor/source.ml/prediction.ml/model_store/${modelType}/${modelNamespace}/${modelName}/${modelVersion}"
    val modelPath = Paths.get(modelParentPath, "saved_model.pb")

    val graphDefBinary: Array[Byte] = Files.readAllBytes(modelPath)
    System.out.println(graphDefBinary.length)

    val graph: Graph = new Graph()
    graph.importGraphDef(graphDefBinary);

    val session: Session = new Session(graph)

    val input = 1.5f
    val inputTensor: Tensor = Tensor.create(input) 
    
    val outputTensor: Tensor = session.runner().feed("x_observed:0", inputTensor).fetch("add:0").run().get(0) 
    val output = new Array[Float](0)
    outputTensor.copyTo(output)
    
    print("Output: " + output)
  }
}
