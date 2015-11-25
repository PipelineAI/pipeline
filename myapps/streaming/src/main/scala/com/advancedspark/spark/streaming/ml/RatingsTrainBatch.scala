package com.advancedspark.spark.streaming.ml

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
import org.apache.spark.mllib.recommendation.ALS
//import org.apache.spark.mllib.recommendation.Rating
import com.advancedspark.spark.streaming.core.Rating
import com.advancedspark.spark.streaming.core.Recommendation

import org.elasticsearch.spark.sql._ 

object RatingsTrainBatch {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(10))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val brokers = "127.0.0.1:9092"
    val topics = Set("item_ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val cassandraConfig = Map("keyspace" -> "advancedspark", "table" -> "item_ratings")
    val esConfig = Map("pushdown" -> "true", "es.nodes" -> "127.0.0.1", "es.port" -> "9200")

    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val itemsDF = sqlContext.read.format("json")
      .load("file:/root/pipeline/datasets/items/items.json")

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

	// TODO:  We're not using any of the stream data at the moment - just the data in cassandra
        //        This is almost like a cron job at the moment.  Not ideal.  Will fix soon.

        // Read all ratings from Cassandra
        // Note:  Cassandra has been initialized through spark-env.sh
        //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
	val allRatingsDF = sqlContext.read.format("org.apache.spark.sql.cassandra")
	  .options(cassandraConfig).load().toDF("userId", "itemId", "rating", "timestamp")

        val allRatings = allRatingsDF.map(rating => 
 	  org.apache.spark.mllib.recommendation.Rating(rating(0).asInstanceOf[Int], rating(1).asInstanceOf[Int], 1)
	)

	// Train the model
	val rank = 10
	val numIterations = 20
	val convergenceThreshold = 0.01

	val model = ALS.train(allRatings, rank, numIterations, convergenceThreshold)

	// Generate top 5 recommendations for everyone in the system (not ideal)
	val recommendationsDF = model.recommendProductsForUsers(5).toDF("userId","recommendationItemIds")
  	  .explode($"recommendationItemIds") { 
	    case Row(recommendations: Seq[Row]) => recommendations.map(recommendation => 
              Recommendation(recommendation(0).asInstanceOf[Int], 
                   	     recommendation(1).asInstanceOf[Int], 
                     	     recommendation(2).asInstanceOf[Double])) 
  	  }
	  .cache()

        val enrichedRecommendationsDF = recommendationsDF.select($"userId", $"itemId", $"confidence")
          .join(itemsDF, $"itemId" === $"itemId")
          .select($"userId", $"itemId", $"title", $"description", $"img", $"confidence").cache()

        enrichedRecommendationsDF.write.format("org.elasticsearch.spark.sql").mode(SaveMode.Overwrite)
	  .options(esConfig).save("advancedspark/personalized-als")

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
