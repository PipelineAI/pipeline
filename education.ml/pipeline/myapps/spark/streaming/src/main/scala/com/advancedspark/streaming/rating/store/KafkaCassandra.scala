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
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.db.CHMCache
import java.net.InetAddress
import java.io.File
import scala.collection.JavaConversions._

object KafkaCassandra {
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

    // Save the DataFrame to Cassandra
    // Note:  Cassandra has been initialized through spark-env.sh
    //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
    val cassandraConfig = Map("keyspace" -> "advancedspark", "table" -> "item_ratings")

    // Kafka Config    
    val brokers = "127.0.0.1:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topics = Set("item_ratings")
 
    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.print()

    ratingsStream.foreachRDD {
      (messages: RDD[(String, String)], batchTime: Time) => {
        // Using the foreachPartition() pattern: 
        //   http://spark.apache.org/docs/latest/streaming-programming-guide.html#performance-tuning
        // to initialize the geoIPResolver (Maxmind DB) for each partition vs. each record
        val ratingsIter = messages.mapPartitions { partitionIter =>
          // A File object pointing to the GeoLite2 database
          val datasetsHome = sys.env("DATASETS_HOME")
          val geoDB = new File(s"${datasetsHome}/geo/GeoLite2-City.mmdb");

          // This creates the DatabaseReader object, which should be reused across lookups
          val geoIPResolver = new DatabaseReader.Builder(geoDB).withCache(new CHMCache()).build();

          val ratingsIterInner = partitionIter.map( message => {
            // Split each _2 element of the (String,String) message tuple into Seq[String]
            val tokens = message._2.split(",")

            // Resolve City from hostname (if provided)
            val geocity =
              if (tokens.length >= 4) {
                val inetAddress = InetAddress.getByName(tokens(3))

                val geoResponse = geoIPResolver.city(inetAddress)

                val cityNames = geoResponse.getCity.getNames

                if (cityNames != null && !cityNames.values.isEmpty)
                  cityNames.values.toArray()(0).toString
                else
                  s"UNKNOWN: ${inetAddress.toString}"
              } else {
                "UNKNOWN"
              }

            RatingGeo(tokens(0).trim.toInt, tokens(1).trim.toInt, tokens(2).trim.toFloat, batchTime.milliseconds, geocity)
          })
         
          ratingsIterInner
        }
          
        val ratingsDF = ratingsIter.toDF("userid", "itemid", "rating", "timestamp", "geocity")

        ratingsDF.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(cassandraConfig)
          .save()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
