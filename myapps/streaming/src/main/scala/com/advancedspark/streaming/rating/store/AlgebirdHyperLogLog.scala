package com.advancedspark.streaming.rating.store

import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import kafka.serializer.StringDecoder
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import org.apache.spark.sql._
import org.apache.spark.sql.types._

// Twitter Algebird HyperLogLog Impl
import com.twitter.algebird.HyperLogLog._
import com.twitter.algebird.HyperLogLogAggregator
import com.twitter.algebird.HyperLogLogMonoid

// Advanced Spark Libs
import com.advancedspark.streaming.rating.core.Rating

object AlgebirdHyperLogLog {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    
    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")

//      newSsc.checkpoint("/root/pipeline/data_work/streaming/")
      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    // Kafka Config
    val brokers = "127.0.0.1:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topics = Set("item_ratings")

    val htmlHome = sys.env("HTML_HOME")

    val itemsDF = sqlContext.read.format("json")
      .load(s"""file:${htmlHome}/advancedspark.com/json/software.json""")

    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    // Setup the Algebird HyperLogLog data struct using 14 bits
    // Note:  this is the same as the Redis implementation
    //        2^14 = 16,384 registers, 0.81% standard error
    // TODO:  Change this to Set[Int, HyperLogLogMonoid] for [itemId, distinctCount]
    val hll = new HyperLogLogMonoid(14)
    
    var globalHll = hll.zero

    val distinctCounts = ratingsStream.mapPartitions(messages => {
      messages.map(message => {
	val itemId = message._2.split(",")(1).trim.toInt
	hll(itemId)
      })
    }).reduce(_ + _)

    val schema = StructType(StructField("itemId", IntegerType, true) :: StructField("approxDistinctCount", LongType, true) :: Nil)

    distinctCounts.foreachRDD(rdd => {
      if (rdd.count() != 0) {
        val batchHll = rdd.first()
        globalHll += batchHll

//        val globalTopK = globalTopKCms.heavyHitters.map(itemId =>
//          (itemId, globalTopKCms.frequency(itemId).estimate)).toSeq.sortBy(_._2).reverse.slice(0, TopK)

//        val globalTopKRDD = sc.parallelize(globalTopK)

//        val globalTopKRDDRows = globalTopKRDD.map(row => Row(row._1, row._2))

//        val globalTopKDF = sqlContext.createDataFrame(globalTopKRDDRows, schema)

//        val enrichedTopKDF =
//          globalTopKDF.join(itemsDF, $"itemId" === $"id")
//            .select($"itemId", $"approxCount", $"title", $"img")
//            .sort($"approxCount" desc)

//        val enrichedTopK = enrichedTopKDF.collect()

        println(s"""Global HLL Distinct Count: ${globalHll.estimatedSize.toInt}""")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
