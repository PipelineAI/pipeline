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
import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction
import com.advancedspark.streaming.rating.core.RatingGeo

object Redis {
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
   
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_._2.split(","))

        // convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token =>
          RatingGeo(token(0).trim.toInt, token(1).trim.toInt, token(2).trim.toInt, batchTime.milliseconds, token(3).trim.toString)
        )

       // increment the exact count for touserid in Redis
        ratings.foreachPartition(ratingsPartitionIter => {
          // TODO:  Fix this.
          //        1) This obviously only works when everything is running on 1 node.
          //        2) This should be using a Jedis Singleton/Pooled connection
          //        3) Explore the spark-redis package (RedisLabs:spark-redis:0.1.0+)
          val jedis = new Jedis("127.0.0.1", 6379)
          val t = jedis.multi()
          ratingsPartitionIter.foreach(rating => {
            val keyExactRatingCount = s"""exact-rating-count:${rating.itemId}"""
	    t.incr(keyExactRatingCount)
            val keyDistinctUserRatingCount = s"""exact-distinct-user-rating-count:${rating.itemId}"""
            t.sadd(keyDistinctUserRatingCount, rating.userId.toString)
	  })
          t.exec()
          jedis.close()
        })

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
