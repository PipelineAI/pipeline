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
import com.advancedspark.streaming.rating.core.RatingGeo

object Parquet {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(5))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val brokers = "localhost:9092"
    val topics = Set("item_ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    
    val dataWorkHome = sys.env("DATA_WORK_HOME")
 
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

        // convert each RDD from the batch into a DataFrame
        val ratingsDF = message.map(_._2.split(",")).map(rating => RatingGeo(rating(0).trim.toInt, rating(1).trim.toInt, rating(2).trim.toInt, batchTime.milliseconds, rating(3).trim.toString)).toDF("userId", "itemId", "rating", "timestamp", "geocity")
        
        ratingsDF.write.format("parquet").partitionBy("rating").save(s"""file:${dataWorkHome}/item_ratings/ratings-partitioned.parquet""")

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
