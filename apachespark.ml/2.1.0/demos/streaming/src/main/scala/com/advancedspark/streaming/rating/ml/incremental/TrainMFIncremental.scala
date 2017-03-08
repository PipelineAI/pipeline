package com.advancedspark.streaming.rating.ml.incremental

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
import com.advancedspark.streaming.rating.ml.Similarity 

import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.streaming.dstream.ConstantInputDStream

import scala.collection.JavaConversions._
import java.util.Collections
import java.util.List

import org.jblas.DoubleMatrix

import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.Seconds
import org.apache.spark.TaskContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.Minutes
import org.apache.spark.sql._
import org.apache.spark.sql.types._

import redis.clients.jedis._

import org.apache.kafka.common.serialization.StringDeserializer

object TrainMFIncremental {
  def main(args: Array[String]) {
   val conf = new SparkConf()
    val session = SparkSession.builder().config(conf).getOrCreate()

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(session.sparkContext, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    // Kafka Config
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "demo.pipeline.io:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Set("item_ratings")

    // Create Kafka Direct Stream Receiver
    val trainingStream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)
    )

    val rank = 5 // suggested number of latent factors
    val maxIterations = 5 // static number of iterations
    val lambdaRegularization = 0.1 // prevent overfitting

    val params = new LatentMatrixFactorizationParams()
      .setRank(rank)
      .setIter(maxIterations)
      .setLambda(lambdaRegularization)
      .setMinRating(0)
      .setMaxRating(1)

    val matrixFactorization = new LatentMatrixFactorization(params)

    // TODO:  Fix these hacks to work around the issue of not having an initialModel
    val initialRatingRDD = session.sparkContext.parallelize(Rating(0L, 0L, 0L) :: Nil)
    val initialModel = None

    // Internally, this setups up additional transformations on the incoming stream
    //   to adjust the weights using GradientDescent
    var (model, numObservations) = LatentMatrixFactorizationModelOps
      .train(initialRatingRDD, params, initialModel, isStreaming = true)

    // Setup the initial transformations from String -> ALS.Rating
    val ratingTrainingStream = trainingStream.map(message => {
      val tokens = message.value().split(",")

      // convert Tokens into RDD[ALS.Rating]
      Rating(tokens(0).trim.toLong, tokens(1).trim.toLong, tokens(2).trim.toFloat)
    })
   
    val jedisPool = new JedisPool(new JedisPoolConfig(), "redis-master", 6379);
    
    ratingTrainingStream.foreachRDD {
      (ratingsBatchRDD: RDD[Rating[Long]], batchTime: Time) => {
      
        if (!ratingsBatchRDD.isEmpty) {
          var (newModel, numObservations) = LatentMatrixFactorizationModelOps
            .train(ratingsBatchRDD, params, Some(model), isStreaming = true)

      	  // TODO:  Hide optimizer so it's not publicly available
  	      // TODO:  Figure out why we're passing in newModel here
          // TODO:  Also,  why are we returning a model here.  
	        // TODO:  And why do we need LatentMatrixFactorizationModelOps
      	  // TODO:  Clean this all up
	        model = matrixFactorization.optimizer
            .train(ratingsBatchRDD, newModel, numObservations)
            .asInstanceOf[StreamingLatentMatrixFactorizationModel]

          val jedis = jedisPool.getResource
          try {           
            // Update Redis in real-time with userFactors and itemFactors
            val userFactors = model.userFactors.filter(_._1 != 0).collect()
            userFactors.foreach(factor => {
              val userId = factor._1
              val factors = factor._2.vector
              jedis.set(s"::user-factors:${userId}", factors.mkString(","))
              
              System.out.println(s"Updated key '::user-factors:${userId}' : ${factors.mkString(",")}")
            })
  
            val itemFactors = model.itemFactors.filter(_._1 != 0).collect()
            itemFactors.foreach(factor => {
              val itemId = factor._1
              val factors = factor._2.vector
              jedis.set(s"::item-factors:${itemId}", factors.mkString(","))
  
              System.out.println(s"TODO: Updated key '::item-factors:${itemId}' : ${factors.mkString(",")}")
            })
  
            // For every (userId, itemId) tuple, calculate prediction
            val allUserItemPredictions =
              for { 
                userFactor <- userFactors 
                itemFactor <- itemFactors
                val prediction = new DoubleMatrix(userFactor._2.vector.map(_.toDouble))
                  .dot(new DoubleMatrix(itemFactor._2.vector.map(_.toDouble)))
              } yield (userFactor._1, itemFactor._1, prediction)
  
            allUserItemPredictions.foreach{ case (userId, itemId, prediction) => 
              jedis.zadd(s"::recommendations:${userId}", prediction, itemId.toString)
            }
  
            System.out.println(s"Updated user-to-item recommendations key '::recommendations:<userId>'")
  
            // Item-to-Item Similarity
  	        //  ::item-similars:${itemId}
            val allItemSimilars = 
              for {
                givenItemFactor <- itemFactors
                similarItemFactor <- itemFactors
                val givenItemFactorsMatrix = new DoubleMatrix(givenItemFactor._2.vector.map(_.toDouble))
                val similarItemFactorsMatrix = new DoubleMatrix(similarItemFactor._2.vector.map(_.toDouble)) 
                val similarity = Similarity.cosineSimilarity(givenItemFactorsMatrix, similarItemFactorsMatrix)
                if (givenItemFactor._1 < similarItemFactor._1)
              }  yield (givenItemFactor._1, similarItemFactor._1, similarity)
   
            allItemSimilars.foreach{ case (givenItemId, similarItemId, similarity) =>
              jedis.zadd(s"::item-similars:${givenItemId}", similarity, similarItemId.toString)
            }
          } finally {
	          if (jedis != null) {
              jedis.close()
	          }
          }

          System.out.println(s"Updated item-to-item similarities key '::item-similars:<itemId>'")
              
          System.out.println(s"Models updated @ time ${batchTime.milliseconds}")
        }
      }
    }
    
    ssc.start()
    ssc.awaitTermination()
  }
}
