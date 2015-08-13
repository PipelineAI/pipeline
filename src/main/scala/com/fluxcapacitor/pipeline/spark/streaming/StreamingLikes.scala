package com.fluxcapacitor.pipeline.spark.streaming

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

case class Like(fromUserId: Int, toUserId: Int)

object StreamingLikes {
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
    
    val brokers = "localhost:9092,localhost:9093"
    val topics = Set("likes")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val likesStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    likesStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        // convert each RDD from the batch into a DataFrame
        val df = message.map(_._2.split(",")).map(like => Like(like(0).trim.toInt, like(1).trim.toInt)).toDF("fromuserid", "touserid", "batchtime")
      
        // add the batch time to the DataFrame
        val dfWithBatchTime = df.withColumn("batch_time", org.apache.spark.sql.functions.lit(batchTime.milliseconds))
      
        // save the DataFrame to Cassandra
        dfWithBatchTime.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> "pipeline", "table" -> "likes"))
          .save()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
