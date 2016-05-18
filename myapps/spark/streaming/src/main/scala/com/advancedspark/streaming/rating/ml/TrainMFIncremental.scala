package com.advancedspark.streaming.rating.ml

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
import com.advancedspark.streaming.ml.incremental.StreamingLatentMatrixFactorization
import com.advancedspark.streaming.ml.incremental.LatentMatrixFactorizationParams
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.streaming.dstream.ConstantInputDStream

// TODO:  Look at Sean Owen's Oryx:  
//https://github.com/OryxProject/oryx/blob/91004a03413eef0fdfd6e75a61b68248d11db0e5/app/oryx-app/src/main/java/com/cloudera/oryx/app/speed/als/ALSSpeedModelManager.java#L193

// This code is based on the following:  https://github.com/brkyvz/streaming-matrix-factorization
object TrainMFIncremental {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(2))
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

    val rank = 20 // suggested number of latent factors
    val maxIterations = 5 // static number of iterations
    val lambdaRegularization = 0.1 // prevent overfitting

    val streamingMFParams = new LatentMatrixFactorizationParams()
      .setRank(rank)
      .setIter(maxIterations)
      .setLambda(lambdaRegularization)
      .setMinRating(0)
      .setMaxRating(1)

    val streamingMF = new StreamingLatentMatrixFactorization(streamingMFParams)

    // Setup the initial transformations from String -> ALS.Rating
    val ratingTrainingStream = trainingStream.map(message => {
      val tokens = message._2.split(",")

      // convert Tokens into RDD[ALS.Rating]
      Rating(tokens(0).trim.toLong, tokens(1).trim.toLong, tokens(2).trim.toFloat)
    })
   
    // Internally, this setups up additional transformations on the incoming stream
    //   to adjust the weights using GradientDescent
    streamingMF.trainOn(ratingTrainingStream)

    ratingTrainingStream.print()

    ratingTrainingStream.foreachRDD {
      (message: RDD[Rating[Long]], batchTime: Time) => {
        message.cache()

        System.out.println("batchTime: " + batchTime)

        if (!streamingMF.model.isEmpty) {
          streamingMF.saveModel(streamingMF.latestModel(), "/root/model.bin")
        }

        message.unpersist()
      }
    }
    
    ssc.start()
    ssc.awaitTermination()
  }
}
