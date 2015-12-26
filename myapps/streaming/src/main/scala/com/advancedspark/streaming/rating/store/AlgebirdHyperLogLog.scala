package com.advancedspark.streaming.rating.store

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

// Redis Client including HyperLogLog Support
import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction

// Twitter Algebird HyperLogLog Impl
import com.twitter.algebird.HyperLogLog._
import com.twitter.algebird.HyperLogLogAggregator

// Advanced Spark Libs
import com.advancedspark.streaming.rating.core.Rating

object AlgebirdHyperLogLog {
  def main(args: Array[String]) {
    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "127.0.0.1")
    
    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    // Kafka Config
    val brokers = "127.0.0.1:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topics = Set("item_ratings")

    // Setup the Algebird HyperLogLog data struct using 14 bits
    // Note:  this is the same as the Redis implementation
    //        2^14 = 16,384 registers, 0.81% standard error
    val hllAggregator = HyperLogLogAggregator.withBits[Int](14)
    var hllCombined = hllAggregator(List()) 

    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_._2.split(","))

	// Convert messageTokens into RDD[Ratings]
        val ratings = tokens.map(token => Rating(token(0).trim.toInt,token(1).trim.toInt,token(2).trim.toInt,batchTime.milliseconds))

        ratings.foreach(rating => {
          hllCombined += hllAggregator(List(rating.itemId))          
          println(s"""Rating itemId: ${rating.itemId}""")
        })

        println(s"""HLL Combined Distinct Count: ${hllCombined.estimatedSize.toInt}""")
	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
