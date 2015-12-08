package com.advancedspark.streaming.rating.agg

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
import org.elasticsearch.spark.sql._ 
import com.advancedspark.streaming.rating.core.Rating
import org.apache.spark.sql.functions._

object TopKItemsByRatingCount {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "127.0.0.1")

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(5))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val brokers = "127.0.0.1:9092"
    val topics = Set("item_ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    
    val cassandraConfig = Map("keyspace" -> "advancedspark", "table" -> "item_ratings")
    val esConfig = Map("pushdown" -> "true", "es.nodes" -> "127.0.0.1", "es.port" -> "9200")

    val datasetsHome = sys.env("DATASETS_HOME")

    val itemsDF = sqlContext.read.format("json").load(s"""file:${datasetsHome}/items/items.json""")

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

	// Ignore any new data and just treat this like a cron job (Demo Purposes)
        
        // Read data from Cassandra
        val itemRatingsDF = sqlContext.read.format("org.apache.spark.sql.cassandra").options(cassandraConfig)
          .load().toDF("userId", "itemId", "rating", "timestamp")

        // Join with Items Reference Data and Find Top K by Rating Count
        val topKItemsByRatingCountDF = itemRatingsDF.join(itemsDF, $"itemId" === $"id").select($"itemId", $"title", $"img")
          .groupBy($"itemId", $"title", $"img").agg(count($"itemId").as("count")).orderBy($"count".desc).limit(5)

        // Write Results to ElasticSearch
        topKItemsByRatingCountDF.write.format("org.elasticsearch.spark.sql").mode(SaveMode.Overwrite).options(esConfig)
          .save("advancedspark/top-items-by-exact-rating-count")
	
        message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
