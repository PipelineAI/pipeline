package com.advancedspark.serving.prediction.keyvalue

import scala.collection.JavaConverters.collectionAsScalaIterableConverter
import scala.collection.immutable.List

import com.netflix.hystrix.HystrixCollapser.CollapsedRequest
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

// TODO  Implement hystrix comllapsing/batching as follows:
//         http://www.nurkiewicz.com/2014/11/batching-collapsing-requests-in-hystrix.html
//         https://github.com/Netflix/Hystrix/wiki/How-To-Use#Collapsing
//

class UserItemBatchPredictionCommand(name: String, fallback: String, 
    timeout: Int, concurrencyPoolSize: Int, rejectionThreshold: Int, 
    collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]])
  extends HystrixCommand[List[Double]](
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
    ) {
  
    def run(): List[Double] = {
//    try{
        // TODO:  Get each of the user::item vector pairs and build up 2 big matrices
        //   collapsedRequests.stream()
        //      .map(CollapsedRequest::getArgument)
        //      .collect(toSet());
        
//            for (request <- collapsedRequests.iterator()) {
          // TODO:  Get each of the user::item vector pairs
          // val userFactors = jedis.get(s"${namespace}:${version}:user-factors:${userId}").split(",").map(_.toDouble)
          // val itemFactors = jedis.get(s"${namespace}:${version}:item-factors:${itemId}").split(",").map(_.toDouble)

          // TODO:  Build 2 big matrices from all the requests
//            }
        
        // TODO:  Multiply the 2 big matrices              
        // val userFactorsMatrix = new DoubleMatrix(userFactors)
        // val itemFactorsMatrix = new DoubleMatrix(itemFactors)
        
        // Calculate the predictionVector 
        // val predictionVector = userFactorsMatrix.mmul(itemFactorsMatrix.transpose()).data

        // TODO:  For each of the requests, grab the appropriate resultScalar from the resultVector             
        //  collapsedRequests.map(
        // for (CollapsedRequest<Double, String> request : requests) {
          // TODO:  For each user::item vector pair, populate the response with the appropriate resultScalar
        // response.put(collapsedRequest.getArgument(), ...);
        // val key = collapsedRequests.toArray()(0).asInstanceOf[CollapsedRequest[Double, String]].getArgument(), 
        System.out.println("collapsedRequests: " + collapsedRequests.size());
        
        val collapsedRequestsArray = new Array[CollapsedRequest[Double, String]](collapsedRequests.size())
        collapsedRequests.toArray(collapsedRequestsArray)
                      
        collapsedRequestsArray.map(request => {          
          // TODO:  For each user::item vector pair, populate the response with the appropriate resultScalar
          // response.put(collapsedRequest.getArgument(), ...);
          // val key = collapsedRequests.toArray()(0).asInstanceOf[CollapsedRequest[Double, String]].getArgument(), 
          0.0
        }).toList
    }
    
    override def getFallback(): List[Double] = {
      val collapsedRequestsArray = new Array[CollapsedRequest[Double, String]](collapsedRequests.size())
      collapsedRequests.toArray(collapsedRequestsArray)
                    
      collapsedRequestsArray.map(request => {
        0.0
      }).toList
    }

/*
class StockTickerPriceCollapsedCommand extends HystrixCollapser[ImmutableMap[Ticker, StockPrice], StockPrice, Ticker] {

    private final StockPriceGateway gateway;
    private final Ticker stock;

    StockTickerPriceCollapsedCommand(StockPriceGateway gateway, Ticker stock) {
        super(HystrixCollapser.Setter.withCollapserKey(HystrixCollapserKey.Factory.asKey("Stock"))
                .andCollapserPropertiesDefaults(HystrixCollapserProperties.Setter().withTimerDelayInMilliseconds(100)));
        this.gateway = gateway;
        this.stock = stock;
    }

    @Override
    public Ticker getRequestArgument() {
        return stock;
    }

    @Override
    protected HystrixCommand<ImmutableMap<Ticker, StockPrice>> createCommand(Collection<CollapsedRequest<StockPrice, Ticker>> collapsedRequests) {
        final Set<Ticker> stocks = collapsedRequests.stream()
                .map(CollapsedRequest::getArgument)
                .collect(toSet());
        return new StockPricesBatchCommand(gateway, stocks);
    }

    @Override
    protected void mapResponseToRequests(ImmutableMap<Ticker, StockPrice> batchResponse, Collection<CollapsedRequest<StockPrice, Ticker>> collapsedRequests) {
        collapsedRequests.forEach(request -> {
            final Ticker ticker = request.getArgument();
            final StockPrice price = batchResponse.get(ticker);
            request.setResponse(price);
        });
    }
    * 
    */
}