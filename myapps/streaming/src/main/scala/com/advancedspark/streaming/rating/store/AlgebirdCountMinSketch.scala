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

import com.twitter.algebird._
import com.twitter.algebird.CMSHasherImplicits._

import com.advancedspark.streaming.rating.core.Rating

object AlgebirdCountMinSketch {
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

    val delta = 1E-3
    val eps = 0.01
    val seed = 1
    val perc = 0.001
    val topK = 5 

    val cms = TopPctCMS.monoid[Long](eps, delta, seed, perc)

  /*
    val filters = args
    val sparkConf = new SparkConf().setAppName("TwitterAlgebirdCMS")
    val ssc = new StreamingContext(sparkConf, Seconds(10))
    val stream = TwitterUtils.createStream(ssc, None, filters, StorageLevel.MEMORY_ONLY_SER_2)

    val users = stream.map(status => status.getUser.getId)

    // val cms = new CountMinSketchMonoid(EPS, DELTA, SEED, PERC)
    val cms = TopPctCMS.monoid[Long](EPS, DELTA, SEED, PERC)
    var globalCMS = cms.zero
    val mm = new MapMonoid[Long, Int]()
    var globalExact = Map[Long, Int]()

    val approxTopUsers = users.mapPartitions(ids => {
      ids.map(id => cms.create(id))
    }).reduce(_ ++ _)

    val exactTopUsers = users.map(id => (id, 1))
      .reduceByKey((a, b) => a + b)

    approxTopUsers.foreachRDD(rdd => {
      if (rdd.count() != 0) {
        val partial = rdd.first()
        val partialTopK = partial.heavyHitters.map(id =>
          (id, partial.frequency(id).estimate)).toSeq.sortBy(_._2).reverse.slice(0, TOPK)
        globalCMS ++= partial
        val globalTopK = globalCMS.heavyHitters.map(id =>
          (id, globalCMS.frequency(id).estimate)).toSeq.sortBy(_._2).reverse.slice(0, TOPK)
        println("Approx heavy hitters at %2.2f%% threshold this batch: %s".format(PERC,
          partialTopK.mkString("[", ",", "]")))
        println("Approx heavy hitters at %2.2f%% threshold overall: %s".format(PERC,
          globalTopK.mkString("[", ",", "]")))
      }
    })

    exactTopUsers.foreachRDD(rdd => {
      if (rdd.count() != 0) {
        val partialMap = rdd.collect().toMap
        val partialTopK = rdd.map(
          {case (id, count) => (count, id)})
          .sortByKey(ascending = false).take(TOPK)
        globalExact = mm.plus(globalExact.toMap, partialMap)
        val globalTopK = globalExact.toSeq.sortBy(_._2).reverse.slice(0, TOPK)
        println("Exact heavy hitters this batch: %s".format(partialTopK.mkString("[", ",", "]")))
        println("Exact heavy hitters overall: %s".format(globalTopK.mkString("[", ",", "]")))
      }
    })
*/ 

    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_._2.split(","))

	// convert messageTokens into RDD[Ratings]
        val ratings = tokens.map(token => Rating(token(0).trim.toInt,token(1).trim.toInt,token(2).trim.toInt,batchTime.milliseconds))

        // TODO:  Update the CMS

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
