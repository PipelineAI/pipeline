package com.advancedspark.streaming.rating.approx

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import kafka.serializer.StringDecoder
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming._
import org.apache.spark.sql._
import org.apache.spark.sql.types._

// Twitter Algebird HyperLogLog Impl
import com.twitter.algebird.HLL
import com.twitter.algebird.HyperLogLog._
import com.twitter.algebird.HyperLogLogAggregator
import com.twitter.algebird.HyperLogLogMonoid

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
import org.apache.kafka.common.serialization.StringDeserializer

object AlgebirdHyperLogLog {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    val session = SparkSession.builder().config(conf).getOrCreate()

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(session.sparkContext, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")
      newSsc.remember(Minutes(10))
      newSsc.checkpoint(s"""/tmp/streaming""")
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

    // Setup the Algebird HyperLogLog data struct using 14 bits
    // Note:  this is the same as the Redis implementation
    //        2^14 = 16,384 registers, 0.81% standard error
    val hllMonoid = new HyperLogLogMonoid(14)
  
    // Merge the current HLL for a given itemId with the new HLLs for the itemId
    def updateStateFunction(newHlls: Seq[HLL], currentHll: Option[HLL]) = {
      val sumHll = hllMonoid.sum(currentHll.getOrElse(hllMonoid.zero) +: newHlls)
      Some(sumHll)
    }

//    def updateStateFunc(batchTime: Time, itemId: Int, newUserIdHlls: Option[Seq[HLL]], state: State[HLL]): Option[(Int, HLL)] = {
//      val sumHll = hllMonoid.sum(state.getOption.getOrElse(hllMonoid.zero) +: newUserIdHlls.getOrElse(Seq(hllMonoid.zero)))
//      state.update(sumHll)
//      Some(itemId, sumHll)
//    }

    // Create (key, value) pairs which is what updateStateByKey expects
    val itemIdHllStream = ratingsStream.map(message => {
      val tokens = message.value().split(",")
      val userId = tokens(0).trim.toInt
      val itemId = tokens(1).trim.toInt
      val userIdHll = hllMonoid.create(userId)
      (itemId, userIdHll)
    })
 
//    val stateSpec = StateSpec.function(updateStateFunc _)
//      .initialState(ssc.sparkContext.parallelize(Seq(hllMonoid.zero)))
      
    // Update the state
    // Spark Streamings internals will organize all HLLs (values) for a given itemId (key)
    //   and pass to the updateStateFunction() method
    val sumItemIdHllStream = itemIdHllStream.updateStateByKey[HLL](updateStateFunction _) 
//      itemIdHllStream.mapWithState[State[HLL], Option[(Int, HLL)]](stateSpec)

    // Format for printing by pulling out the estimatedSize from the HLL
    val sumItemIdApproxDistinctCountStream = sumItemIdHllStream.map(itemIdHll => (itemIdHll._1, itemIdHll._2.estimatedSize.toLong))

    val schema = StructType(StructField("itemId", IntegerType, true) :: StructField("approxDistinctCount", LongType, true) :: Nil)

    val sumItemIdApproxDistrinctCountRowStream = sumItemIdApproxDistinctCountStream.map(rdd => (rdd._1, rdd._2))

    sumItemIdApproxDistrinctCountRowStream.foreachRDD(rdd => {
      import session.sqlContext.implicits._
      val sumItemIdApproxDistinctCountRowsDF = rdd.toDF("itemId", "approxDistinctCount")

      val enrichedDF =
        sumItemIdApproxDistinctCountRowsDF.join(itemsDF, $"itemId" === $"id")
          .select($"itemId", $"title", $"approxDistinctCount")
          .sort($"approxDistinctCount" desc)
          .limit(5)

        val enriched = enrichedDF.collect()

        println(s"""Approx Distinct Count HLL: ${enriched.mkString("[",",","]")}""")
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
