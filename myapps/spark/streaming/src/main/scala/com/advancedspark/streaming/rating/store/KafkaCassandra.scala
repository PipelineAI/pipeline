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

    // Cassandra Config
    val cassandraConfig = Map("keyspace" -> "advancedspark", "table" -> "item_ratings")

    // Kafka Config    
    val brokers = "127.0.0.1:9092"
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val topics = Set("item_ratings")
 
    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.print()

    ratingsStream.foreachRDD {
      (message: RDD[(String, String)], batchTime: Time) => {
        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_._2.split(","))

	// convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token => {
          // Resolve City from hostname (if provided)
          val geocity =
            if (token.length >= 4) {
	      val datasetsHome = sys.env("DATASETS_HOME")

	      // **********************************************************************************************************
	      // This is not the best implementation as this is creating a new geoIPResolver per record
	      // TODO:  Use foreachPartition() instead per the following URL:
	      //          http://stackoverflow.com/questions/36951207/spark-scala-get-data-back-from-rdd-foreachpartition 
	      // **********************************************************************************************************

   	      // A File object pointing to the GeoLite2 database
    	      val geoDB = new File(s"${datasetsHome}/geo/GeoLite2-City.mmdb");

  	      // This creates the DatabaseReader object, which should be reused across lookups
	      val geoIPResolver = new DatabaseReader.Builder(geoDB).withCache(new CHMCache()).build();
              val inetAddress = InetAddress.getByName(token(3))

              val geoResponse = geoIPResolver.city(inetAddress)
              val cityNames = geoResponse.getCity.getNames
              if (cityNames != null && !cityNames.values.isEmpty)
                cityNames.values.toArray()(0).toString
              else
                s"UNKNOWN: ${inetAddress.toString}"
            } else {
              "UNKNOWN"
            }

          RatingGeo(token(0).trim.toInt,token(1).trim.toInt, token(2).trim.toFloat, batchTime.milliseconds, geocity)
        })
  
        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through spark-env.sh
        //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
        val ratingsDF = ratings.toDF("userid", "itemid", "rating", "timestamp", "geocity")

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
