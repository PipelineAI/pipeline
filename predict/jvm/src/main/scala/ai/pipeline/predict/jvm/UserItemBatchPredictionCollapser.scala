package ai.pipeline.predict.jvm

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import com.netflix.hystrix.HystrixCollapser
import com.netflix.hystrix.HystrixCollapser.CollapsedRequest
import com.netflix.hystrix.HystrixCollapser.Scope
import com.netflix.hystrix.HystrixCollapserKey
import com.netflix.hystrix.HystrixCollapserProperties
import com.netflix.hystrix.HystrixCommand

/**
 * Sample {@link HystrixCollapser} that automatically batches multiple requests to execute()/queue()
 * into a single {@link HystrixCommand} execution for all requests within the defined batch (time or size).
 */
class UserItemBatchPredictionCollapser(runtimeType: String,
                                       commandName: String, 
                                       namespace: String, 
                                       version: String, 
                                       fallback: Double,
                                       timeout: Int, 
                                       concurrencyPoolSize: Int,  
                                       rejectionThreshold: Int,                 
                                       userId: String, 
                                       itemId: String)
  extends HystrixCollapser[Map[String, Double], Double, String](
    HystrixCollapser.Setter
      .withCollapserKey(HystrixCollapserKey.Factory.asKey(commandName)).andScope(Scope.GLOBAL)
      .andCollapserPropertiesDefaults(HystrixCollapserProperties.
        Setter().withTimerDelayInMilliseconds(10).withRequestCacheEnabled(false))) {
      
  override def getRequestArgument(): String = s"${userId}:${itemId}" 

  override def createCommand(collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]]): HystrixCommand[Map[String, Double]] = {
    new UserItemBatchPredictionCommand(runtimeType, commandName, namespace, version, userId, itemId, fallback, timeout, concurrencyPoolSize, rejectionThreshold, collapsedRequests)    
  }

  override def mapResponseToRequests(batchResponse: Map[String, Double], collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]]): Unit = {   
  	collapsedRequests.asScala.foreach(request => {
      val userIdItemId = request.getArgument()
      val prediction = batchResponse.get(userIdItemId)
      request.setResponse(prediction.get)
    })
  }
}
