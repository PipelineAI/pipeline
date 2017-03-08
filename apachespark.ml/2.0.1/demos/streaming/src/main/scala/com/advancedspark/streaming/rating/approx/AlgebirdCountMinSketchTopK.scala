package com.advancedspark.streaming.rating.approx

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

// Twitter Algebird CountMin Sketch Impl
import com.twitter.algebird.TopPctCMS
import com.twitter.algebird.CMSHasherImplicits._
import com.madhukaraphatak.sizeof.SizeEstimator
import org.apache.kafka.common.serialization.StringDeserializer

object AlgebirdCountMinSketchTopK {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    val session = SparkSession.builder().config(conf).getOrCreate()

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(session.sparkContext, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")
      newSsc.remember(Minutes(10))
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

    val htmlHome = sys.env("GITHUB_REPO_NAME")
 
    val itemsDF = session.sqlContext.read.format("json")
      .load(s"""file:${htmlHome}/advancedspark.com/json/actors.json""")

    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)
    )

    // Setup the Algebird CountMin Sketch data struct
    val eps = 0.001
    val delta = 1E-10
    val seed = 1
    val minTopKPctOfTotal = 1E-10
    val TopK = 5
    
    val topKCms = TopPctCMS.monoid[Int](eps, delta, seed, minTopKPctOfTotal)
    var globalTopKCms = topKCms.zero

    // Messages come in off the Kafka source as follows:
    //   (userId, itemId, rating)
    val counts = ratingsStream.mapPartitions(messages => {
      messages.map(message => {
	val itemId = message.value().split(",")(1).trim.toInt
	topKCms.create(itemId)
      })
    }).reduce(_ ++ _)

    counts.foreachRDD(rdd => {
      if (rdd.count() != 0) {
        val batchTopKCms = rdd.first()
      
        globalTopKCms ++= batchTopKCms
        
        val globalTopK = globalTopKCms.heavyHitters.map(itemId => 
          (itemId, globalTopKCms.frequency(itemId).estimate)).toSeq.sortBy(_._2).reverse.slice(0, TopK)
      
        import session.sqlContext.implicits._
        val globalTopKDF = session.sparkContext.parallelize(globalTopK).toDF("itemId", "approxCount")

	      val enrichedTopK =
          globalTopKDF.join(itemsDF, $"itemId" === $"id")
            .select($"itemId", $"approxCount", $"title")
            .sort($"approxCount" desc)
            .collect()
       
        println(s"""Top 5 Heavy Hitters CMS: ${enrichedTopK.mkString("[",",","]")}, CMS Size: ${SizeEstimator.estimate(globalTopKCms)}""")
        //, HashMap Size: ${SizeEstimator.estimatedSized(globalHashSet)}""")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
