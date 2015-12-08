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
import org.apache.spark.mllib.recommendation.ALS
import com.advancedspark.streaming.rating.core.Rating

import org.elasticsearch.spark.sql._ 

object TrainALSBatch {
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

    val datasetsHome = sys.env("DATASETS_HOME")

    val itemsDF = sqlContext.read.format("json")
      .load(s"""file:${datasetsHome}/items/items.json""")

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

        // Convert to Spark ML Recommendation Ratings
        val allRecommendationRatings = allRatingsDF.map(rating => 
 	  org.apache.spark.mllib.recommendation.Rating(rating(0).asInstanceOf[Int], rating(1).asInstanceOf[Int], 1)
	)

	// Train the model
	val rank = 10
	val numIterations = 20
	val convergenceThreshold = 0.01

	val model = ALS.train(allRecommendationRatings, rank, numIterations, convergenceThreshold)

	// Generate top 5 recommendations for everyone in the system (not ideal)
	val recommendationsDF = model.recommendProductsForUsers(5).toDF("id","recommendationItemIds")
  	  .explode($"recommendationItemIds") { 
	    case Row(recommendations: Seq[Row]) => recommendations.map(recommendation => 
              Recommendation(recommendation(0).asInstanceOf[Int], 
                   	     recommendation(1).asInstanceOf[Int], 
                     	     recommendation(2).asInstanceOf[Double])) 
  	  }

        val enrichedRecommendationsDF = recommendationsDF.select($"userId", $"itemId", $"confidence")
          .join(itemsDF, $"itemId" === $"id")
          .select($"userId", $"itemId", $"title", $"img", $"confidence")

        enrichedRecommendationsDF.write.format("org.elasticsearch.spark.sql").mode(SaveMode.Overwrite)
	  .options(esConfig).save("advancedspark/personalized-als")

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
