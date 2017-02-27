package com.advancedspark.serving.prediction.tensorflow

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class TensorflowNativeCommand(host: String, port: Int, name: String, inputs: Map[String, Any],
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
  def run(): String = {
    try{
//      // https://medium.com/google-cloud/how-to-invoke-a-trained-tensorflow-model-from-java-programs-27ed5f4f502d#.pcz10o9h0
//      // https://github.com/tensorflow/tensorflow/blob/master/tensorflow/java/src/main/java/org/tensorflow/examples/LabelImage.java
//
//      // try to predict for two (2) sets of inputs.
//      val inputs: Tensor = new Tensor(Tensorflow.DT_FLOAT, new TensorShape(2,5))
//      val x: FloatBuffer = inputs.createBuffer()
//      x.put(new float[]{-6.0f,22.0f,383.0f,27.781754111198122f,-6.5f})
//      x.put(new float[]{66.0f,22.0f,2422.0f,45.72160947712418f,0.4f})
//      val keepall: Tensor = new Tensor(tensorflow.DT_FLOAT, new TensorShape(2,1))
//      ((FloatBuffer)keepall.createBuffer()).put(new float[]{1f, 1f})
//      val outputs: TensorVector = new TensorVector()
//      // to predict each time, pass in values for placeholders
//      outputs.resize(0);
//      val sess = session.Run(new StringTensorPairVector(new String[] {“Placeholder”, “Placeholder_2”}, new Tensor[] {inputs, keepall})
//      new StringVector(“Sigmoid”), new StringVector(), outputs)
//      if (!sess.ok()) {
//        throw new RuntimeException(s.error_message().getString())
//      }
//      // this is how you get back the predicted value from outputs
//      val output: FloatBuffer = outputs.get(0).createBuffer()
//      for (int k=0; k < output.limit(); ++k){
//        System.out.println(“prediction=” + output.get(k))
//      }      
//      
//      s"""${results}"""
      
      s"""foo"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  override def getFallback(): String = {
    s"""${fallback}"""
  }
}
