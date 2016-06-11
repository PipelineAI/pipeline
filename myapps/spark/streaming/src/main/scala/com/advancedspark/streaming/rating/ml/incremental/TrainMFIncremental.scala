package com.advancedspark.streaming.rating.ml.incremental

import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import com.advancedspark.streaming.rating.ml.incremental.model.StreamingLatentMatrixFactorizationModel
import com.advancedspark.streaming.rating.ml.incremental.model.LatentMatrixFactorizationModelOps
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.streaming.dstream.ConstantInputDStream

object TrainMFIncremental {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(20))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val brokers = "127.0.0.1:9092"
    val topics = Set("item_ratings")

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val trainingStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val rank = 10 // suggested number of latent factors
    val maxIterations = 5 // static number of iterations
    val lambdaRegularization = 0.1 // prevent overfitting

    val params = new LatentMatrixFactorizationParams()
      .setRank(rank)
      .setIter(maxIterations)
      .setLambda(lambdaRegularization)
      .setMinRating(0)
      .setMaxRating(1)

    val matrixFactorization = new LatentMatrixFactorization(params)

    // TODO:  fix these hacks to work around the issue of not having an initialModel
    val initialRatingRDD = sc.parallelize(Rating(0L, 0L, 0L) :: Nil)
    val initialModel = None

    // Internally, this setups up additional transformations on the incoming stream
    //   to adjust the weights using GradientDescent
    var (model, numObservations) = LatentMatrixFactorizationModelOps.train(initialRatingRDD, params, initialModel, isStreaming = true)

    // Setup the initial transformations from String -> ALS.Rating
    val ratingTrainingStream = trainingStream.map(message => {
      val tokens = message._2.split(",")

      // convert Tokens into RDD[ALS.Rating]
      Rating(tokens(0).trim.toLong, tokens(1).trim.toLong, tokens(2).trim.toFloat)
    })
   
   // ratingTrainingStream.print()

    ratingTrainingStream.foreachRDD {
      (ratingsBatchRDD: RDD[Rating[Long]], batchTime: Time) => {
      
        //System.out.println("batchTime: " + batchTime)
        //System.out.println("ratingsBatchRDD: " + ratingsBatchRDD)

        if (!ratingsBatchRDD.isEmpty) {
          var (newModel, numObservations) = LatentMatrixFactorizationModelOps.train(ratingsBatchRDD, params, Some(model), isStreaming = true)

	  // TODO:  Hide optimizer so it's not publicly available
  	  // TODO:  Figure out why we're passing in newModel here
          // TODO:  Also,  why are we returning a model here.  
	  // TODO:  And why do we need LatentMatrixFactorizationModelOps
	  // TODO:  Clean this all up
	  model = matrixFactorization.optimizer.train(ratingsBatchRDD, newModel, numObservations).asInstanceOf[StreamingLatentMatrixFactorizationModel]

          //val modelFilename = s"/tmp/live-recommendations/spark-1.6.1/streaming-mf/$batchTime.bin"
          //matrixFactorization.saveModel(modelFilename)

          /* USING TEXT VERSION FOR DEBUG/TESTING/GROK PURPOSES */
          val modelTextFilename = s"/tmp/live-recommendations/spark-1.6.1/text-debug-only/streaming-mf/${batchTime.milliseconds}"
          matrixFactorization.saveText(model, modelTextFilename)

          System.out.println(s"Model updated @ time ${batchTime.milliseconds} : $model : $modelTextFilename ")
        }
      }
    }
    
    ssc.start()
    ssc.awaitTermination()
  }
}
