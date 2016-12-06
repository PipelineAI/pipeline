package com.advancedspark.serving.prediction

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import org.jblas.DoubleMatrix

import scala.util.parsing.json._

import redis.clients.jedis._

import collection.JavaConverters._
import scala.collection.immutable.List
//
// TODO  Imlpement hystrix comllapsing/batching as follows:
//         http://www.nurkiewicz.com/2014/11/batching-collapsing-requests-in-hystrix.html
//         https://github.com/Netflix/Hystrix/wiki/How-To-Use#Collapsing
//
class UserItemBatchPredictionCommand(
      jedis: Jedis, namespace: String, version: String, userIds: Array[String], itemIds: Array[String])
    extends HystrixCommand[Array[Double]](HystrixCommandGroupKey.Factory.asKey("UserItemBatchPrediction")) {

  @throws(classOf[java.io.IOException])
  def get(url: String) = scala.io.Source.fromURL(url).mkString

  def run(): Array[Double] = {
    try{
      val userFactorsArray = userIds.map(userId => 
        jedis.get(s"${namespace}:${version}:user-factors:${userId}").split(",").map(_.toDouble)
      )
      val itemFactorsArray = itemIds.map(itemId =>
        jedis.get(s"${namespace}:${version}:item-factors:${itemId}").split(",").map(_.toDouble)
      ) 

      val userFactorsMatrix = new DoubleMatrix(userFactorsArray)
      val itemFactorsMatrix = new DoubleMatrix(itemFactorsArray)
    
      System.out.println(s"userFactorsRows: ${userFactorsMatrix.columns}")
      System.out.println(s"itemFactorsCols: ${itemFactorsMatrix.rows}")   

      // Calculate batch predictions - returns single vector 
      userFactorsMatrix.mmul(itemFactorsMatrix.transpose()).data
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Array[Double] = {
    System.out.println("UserItemBatchPrediction Source is Down!  Fallback!!")

    Array(0.0)
  }
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
}
*/
