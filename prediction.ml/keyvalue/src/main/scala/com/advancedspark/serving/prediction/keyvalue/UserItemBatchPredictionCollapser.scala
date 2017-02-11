package com.advancedspark.serving.prediction.keyvalue

import scala.collection.JavaConverters.collectionAsScalaIterableConverter

import com.netflix.hystrix.HystrixCollapser
import com.netflix.hystrix.HystrixCollapser.CollapsedRequest
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCollapserKey
import com.netflix.hystrix.HystrixCollapser.Scope
import com.netflix.hystrix.HystrixCollapserProperties

/**
 * Sample {@link HystrixCollapser} that automatically batches multiple requests to execute()/queue()
 * into a single {@link HystrixCommand} execution for all requests within the defined batch (time or size).
 */
class UserItemBatchPredictionCollapser(name: String, fallback: String, timeout: Int, concurrencyPoolSize: Int,  
    rejectionThreshold: Int, key: String)
  extends HystrixCollapser[List[Double], Double, String](
    HystrixCollapser.Setter
      .withCollapserKey(HystrixCollapserKey.Factory.asKey(name)).andScope(Scope.GLOBAL)
      .andCollapserPropertiesDefaults(HystrixCollapserProperties.
        Setter().withTimerDelayInMilliseconds(5).withRequestCacheEnabled(false))) {
      
  override def getRequestArgument(): String = key

  override def createCommand(collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]]): HystrixCommand[List[Double]] = {
    new UserItemBatchPredictionCommand(name, fallback, timeout, concurrencyPoolSize, rejectionThreshold, collapsedRequests)    
  }

  override def mapResponseToRequests(batchResponse: List[Double], collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]]): Unit = {
    var idx = 0   
  	collapsedRequests.asScala.foreach(request => {
      //String userIdItemIdTuple = request.getArgument();
      val prediction = batchResponse(idx)
      request.setResponse(prediction)
      idx = idx + 1
    })
  }
}