package ai.pipeline.predict.jvm

import org.jblas.DoubleMatrix

import com.netflix.hystrix.HystrixCollapser.CollapsedRequest
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixThreadPoolKey
import com.netflix.hystrix.HystrixThreadPoolProperties

class UserItemBatchPredictionCommand(runtimeType: String,
                                     commandName: String, 
                                     namespace: String, 
                                     version: String, 
                                     userId: String, 
                                     itemId: String, 
                                     fallback: Double,
                                     timeout: Int, 
                                     concurrencyPoolSize: Int, 
                                     rejectionThreshold: Int, 
                                     collapsedRequests: java.util.Collection[CollapsedRequest[Double, String]])
  extends HystrixCommand[Map[String, Double]](
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
    ) {
  
    def run(): Map[String, Double] = {    
//      System.out.println("collapsedRequests: " + collapsedRequests.size());
     
      val collapsedRequestsArray = new Array[CollapsedRequest[Double, String]](collapsedRequests.size())
      collapsedRequests.toArray(collapsedRequestsArray)

      val numRequests = collapsedRequestsArray.length
      // TODO:  Change this to the actual number of features that are returned
      // Note:  The matrix dimensions must be matrix-multiply-friendly or the fallback will kick in.
      val numFactors = 1
      
      var allUserFactorsMatrix = DoubleMatrix.zeros(0, numFactors)
      var allItemFactorsMatrix = DoubleMatrix.zeros(numFactors, 0)

      // Build up big userFactors matrix from all requests
      collapsedRequestsArray.foreach(request => {
        // TODO:  retrieve the actual factor matrices
        val userFactors = Array(0.99d)
        val itemFactors = Array(0.90d)
      
        // Convert to DoubleMatrix
        val userFactorsMatrix = new DoubleMatrix(userFactors)
        val itemFactorsMatrix = new DoubleMatrix(itemFactors)
    
//        System.out.println("userFactorsMatrix: " + userFactorsMatrix)
//        System.out.println("userFactorsMatrix length: " + userFactorsMatrix.length)
//        System.out.println("userFactorsMatrix numRows: " + userFactorsMatrix.getRows)
//        System.out.println("userFactorsMatrix numColumns: " + userFactorsMatrix.getColumns)
//
//        System.out.println("itemFactorsMatrix: " + itemFactorsMatrix)
//        System.out.println("itemFactorsMatrix length: " + itemFactorsMatrix.length)
//        System.out.println("itemFactorsMatrix numRows: " + itemFactorsMatrix.getRows)
//        System.out.println("itemFactorsMatrix numColumns: " + itemFactorsMatrix.getColumns)
              
        allUserFactorsMatrix = DoubleMatrix.concatVertically(allUserFactorsMatrix, userFactorsMatrix)
        allItemFactorsMatrix = DoubleMatrix.concatHorizontally(allItemFactorsMatrix, itemFactorsMatrix)

//        System.out.println("allUserFactorsMatrix: " + allUserFactorsMatrix)
//        System.out.println("allUserFactorsMatrix length: " + allUserFactorsMatrix.length)
//        System.out.println("allUserFactorsMatrix numRows: " + allUserFactorsMatrix.getRows)
//        System.out.println("allUserFactorsMatrix numColumns: " + allUserFactorsMatrix.getColumns)
//        
//        System.out.println("allItemFactorsMatrix: " + allItemFactorsMatrix)
//        System.out.println("allItemFactorsMatrix length: " + allItemFactorsMatrix.length)
//        System.out.println("allItemFactorsMatrix numRows: " + allItemFactorsMatrix.getRows)
//        System.out.println("allItemFactorsMatrix numColumns: " + allItemFactorsMatrix.getColumns)
      })
      
      // Big matrix multiply of userFactors x itemFactors   
      val predictionsMatrix = allUserFactorsMatrix.mmul(allItemFactorsMatrix)
//      System.out.println("predictionsMatrix: " + predictionsMatrix)
//      System.out.println("predictionsMatrix length: " + predictionsMatrix.length)
//      System.out.println("predictionsMatrix numRows: " + predictionsMatrix.getRows)
//      System.out.println("predictionsMatrix numColumns: " + predictionsMatrix.getColumns)
//
//      System.out.println("predictionsMatrix rows: " + predictionsMatrix.rowsAsList())
//      System.out.println("predictionsMatrix columns: " + predictionsMatrix.columnsAsList())
      
      // Get the diagonal vector
      // Linear Algebra refresher:  https://en.wikipedia.org/wiki/Matrix_multiplication
      val predictions: DoubleMatrix = predictionsMatrix.diag()
//      System.out.println("predictions: " + predictions)
//      System.out.println("predictions length: " + predictions.length)
//      System.out.println("predictions numRows: " + predictions.getRows)
//      System.out.println("predictions numColumns: " + predictions.getColumns)

      // TODO: Refactor this
      var idx = -1          
      collapsedRequestsArray.map(request => {
        idx = idx + 1
//        System.out.println("prediction: " + predictions.get(idx))
        (request.getArgument -> predictions.get(idx))
      }).toMap
    }
    
    override def getFallback(): Map[String, Double] = {
      val collapsedRequestsArray = new Array[CollapsedRequest[Double, String]](collapsedRequests.size())
      collapsedRequests.toArray(collapsedRequestsArray)
                      
      val responseMap = collapsedRequestsArray.map(request => {          
        val prediction = fallback
//        System.out.println("FALLBACK prediction: " + prediction);
        (request.getArgument -> prediction)
      }).toMap
        
      responseMap
    }
}
