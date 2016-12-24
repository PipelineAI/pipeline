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
import com.advancedspark.streaming.rating.ml.Similarity 

import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.streaming.dstream.ConstantInputDStream

import scala.collection.JavaConversions._
import java.util.Collections
import java.util.Collection
import java.util.List

import org.jblas.DoubleMatrix

import com.netflix.dyno.jedis._
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.connectionpool.exception.DynoException
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.dyno.connectionpool.impl.ConnectionContextImpl
import com.netflix.dyno.connectionpool.impl.OperationResultImpl
import com.netflix.dyno.connectionpool.impl.utils.ZipUtils

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

    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, 
                                          "auto.offset.reset" -> "smallest")

    val trainingStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

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
    val initialRatingRDD = sc.parallelize(Rating(0L, 0L, 0L) :: Nil)
    val initialModel = None

    // Internally, this setups up additional transformations on the incoming stream
    //   to adjust the weights using GradientDescent
    var (model, numObservations) = LatentMatrixFactorizationModelOps
      .train(initialRatingRDD, params, initialModel, isStreaming = true)

    // Setup the initial transformations from String -> ALS.Rating
    val ratingTrainingStream = trainingStream.map(message => {
      val tokens = message._2.split(",")

      // convert Tokens into RDD[ALS.Rating]
      Rating(tokens(0).trim.toLong, tokens(1).trim.toLong, tokens(2).trim.toFloat)
    })
   
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

          // Update Redis in real-time with userFactors and itemFactors
          val userFactors = model.userFactors.filter(_._1 != 0).collect()
          userFactors.foreach(factor => {
            val userId = factor._1
            val factors = factor._2.vector
            DynomiteOps.dynoClient.set(s"::user-factors:${userId}", factors.mkString(","))
            System.out.println(s"Updated key '::user-factors:${userId}' : ${factors.mkString(",")}")
          })

          val itemFactors = model.itemFactors.filter(_._1 != 0).collect()
          itemFactors.foreach(factor => {
            val itemId = factor._1
            val factors = factor._2.vector
            DynomiteOps.dynoClient.set(s"::item-factors:${itemId}", factors.mkString(","))
            System.out.println(s"Updated key '::item-factors:${itemId}' : ${factors.mkString(",")}")
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
            DynomiteOps.dynoClient.zadd(s"::recommendations:${userId}", prediction, itemId.toString)
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
            DynomiteOps.dynoClient.zadd(s"::item-similars:${givenItemId}", similarity, similarItemId.toString)
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

object DynomiteOps {
  val localhostHost = new Host("127.0.0.1", Host.Status.Up)
  val localhostToken = new HostToken(100000L, localhostHost)

  val localhostHostSupplier = new HostSupplier() {
    @Override
    def getHosts(): Collection[Host] = {
      Collections.singletonList(localhostHost)
    }
  }

  val localhostTokenMapSupplier = new TokenMapSupplier() {
    @Override
    def getTokens(activeHosts: java.util.Set[Host]): List[HostToken] = {
      Collections.singletonList(localhostToken)
    }

    @Override
    def getTokenForHost(host: Host, activeHosts: java.util.Set[Host]): HostToken = {
      return localhostToken
    }
  }

  val redisPort = 6379
  val dynoClient = new DynoJedisClient.Builder()
             .withApplicationName("pipeline")
             .withDynomiteClusterName("pipeline-dynomite")
             .withHostSupplier(localhostHostSupplier)
             .withCPConfig(new ConnectionPoolConfigurationImpl("localhostTokenMapSupplier")
                .withTokenSupplier(localhostTokenMapSupplier))
             .withPort(redisPort)
             .build()
}
