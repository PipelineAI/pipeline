package ai.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.apache.http.entity.ContentType
import org.apache.http.message.BasicHeader

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

// TODO:  Add the ZipKin Headers to HttpEvaluationCommand
class HttpEvaluationCommand(modelUrl: String,
                            modelName: String,
                            modelTag: String,
                            modelType: String,
                            modelRuntime: String,
                            modelChip: String,
                            requestMethod: String,
                            requestBody: String,
                            fallback: String,
                            timeout: Int,
                            concurrencyPoolSize: Int,
                            rejectionThreshold: Int,
                            xreq: String,
                            xtraceid: String,
                            xspanid: String,
                            xparentspanid: String,
                            xsampled: String,
                            xflags: String,
                            xotspan: String)
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
       // TODO: Refactor this
       val xreqHeader = new BasicHeader("x-request-id", xreq)
       val xtraceidHeader = new BasicHeader("x-b3-traceid", xtraceid)
       val xspanidHeader = new BasicHeader("x-b3-spanid", xspanid)
       val xparentspanidHeader = new BasicHeader("x-b3-parentspanid", xparentspanid)
       val xsampledHeader = new BasicHeader("x-b3-sampled", xsampled)
       val xflagsHeader = new BasicHeader("x-b3-flags", xflags)
       val xotspanHeader = new BasicHeader("x-ot-span-context", xotspan)

      System.out.println(s"""HttpEvaluationCommand: before""")

       // TODO:  Don't hard code RequestMethod/Post and ContentType/Json
       val results = org.apache.http.client.fluent.Request
         .Post(modelUrl)
         .bodyString(requestBody, ContentType.APPLICATION_JSON)
         .setHeaders(xreqHeader, xtraceidHeader, xspanidHeader, xparentspanidHeader, xsampledHeader, xflagsHeader, xotspanHeader)
         .execute()
         .returnContent();

      System.out.println(s"""HttpEvaluationCommand: ${results}""")

      s"""${results}"""
    } catch {
       case e: Throwable => {
         // System.out.println(e)
         throw e
       }
    }
  }

  override def getFallback(): String = {
    s"""${fallback}"""
  }
}
