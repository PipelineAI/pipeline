package com.advancedspark.streaming.rating.approx

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
import org.apache.spark.streaming.Minutes
import org.apache.spark.sql._
import org.apache.spark.sql.types._

// Twitter Algebird CountMin Sketch Impl
import com.twitter.algebird.TopPctCMS
import com.twitter.algebird.CMSHasherImplicits._

object AlgebirdCountMinSketchTopK {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    
    val sc = SparkContext.getOrCreate(conf)

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")
      newSsc.remember(Minutes(10))
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

    // Setup the Algebird CountMin Sketch data struct
    val eps = 0.001
    val delta = 1E-10
    val seed = 1
    val minTopKPctOfTotal = 1E-10
    val TopK = 5
    
    val topKCms = TopPctCMS.monoid[Int](eps, delta, seed, minTopKPctOfTotal)
    var globalTopKCms = topKCms.zero

    val counts = ratingsStream.mapPartitions(messages => {
      messages.map(message => {
	val itemId = message._2.split(",")(1).trim.toInt
	topKCms.create(itemId)
      })
    }).reduce(_ ++ _)

    val schema = StructType(StructField("itemId", IntegerType, true) :: StructField("approxCount", LongType, true) :: Nil)
 
    counts.foreachRDD(rdd => {
      if (rdd.count() != 0) {
        val batchTopKCms = rdd.first()
        globalTopKCms ++= batchTopKCms
        
        val globalTopK = globalTopKCms.heavyHitters.map(itemId => 
          (itemId, globalTopKCms.frequency(itemId).estimate)).toSeq.sortBy(_._2).reverse.slice(0, TopK)
      
        val globalTopKRDD = sc.parallelize(globalTopK)

        val globalTopKRDDRows = globalTopKRDD.map(row => Row(row._1, row._2))

        val globalTopKDF = sqlContext.createDataFrame(globalTopKRDDRows, schema)

	val enrichedTopKDF =
          globalTopKDF.join(itemsDF, $"itemId" === $"id")
            .select($"itemId", $"approxCount", $"title", $"img")
            .sort($"approxCount" desc)
       
	val enrichedTopK = enrichedTopKDF.collect()

        println(s"""Top 5 Heavy Hitters CMS: ${enrichedTopK.mkString("[",",","]")}""")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
