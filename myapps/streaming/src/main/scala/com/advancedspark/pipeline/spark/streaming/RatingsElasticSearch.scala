package com.advancedspark.pipeline.spark.streaming

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

object RatingsElasticSearch {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(30))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val brokers = "localhost:9092"
    val topics = Set("ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    val esConfig = Map("pushdown" -> "true", "es.nodes" -> "demo.fluxcapacitor.com", "es.port" -> "9200")

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_._2.split(","))

        // convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token => Rating(token(0).trim.toInt,token(1).trim.toInt,token(2).trim.toInt,batchTime.milliseconds))

        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through spark-env.sh
        //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
        val ratingsDF = ratings.toDF("fromuserid", "touserid", "rating", "batchtime")

	ratingsDF.write.format("org.elasticsearch.spark.sql")
    	  .mode(SaveMode.Overwrite)
	  .options(esConfig)
   	  .save("fluxcapacitor/ratings")

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
