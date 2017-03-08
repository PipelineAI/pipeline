package com.advancedspark.streaming.rating.store

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
import com.advancedspark.streaming.rating.core.RatingGeo

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
import shaded.parquet.org.codehaus.jackson.map.deser.StdDeserializer.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord

object ElasticSearch {
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
    val ratingsStream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)
    )

    val esConfig = Map("pushdown" -> "true", "es.nodes" -> "127.0.0.1", "es.port" -> "9200")

    ratingsStream.foreachRDD {
      (message: RDD[ConsumerRecord[String, String]], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_.value().split(","))

        // convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token =>
          RatingGeo(token(0).trim.toInt, token(1).trim.toInt, token(2).trim.toInt, batchTime.milliseconds, token(3).trim.toString)
        )

        // save the DataFrame to ElasticSearch
        import session.sqlContext.implicits._
        val ratingsDF = ratings.toDF("userId", "itemId", "rating", "timestamp", "geocity")

	ratingsDF.write.format("org.elasticsearch.spark.sql")
    	  .mode(SaveMode.Append)
	  .options(esConfig)
   	  .save("advancedspark/item_ratings")

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
