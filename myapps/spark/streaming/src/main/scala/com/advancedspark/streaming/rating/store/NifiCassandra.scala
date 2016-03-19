package com.advancedspark.streaming.rating.store

import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.Row
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time
import com.advancedspark.streaming.rating.core.Rating
import org.apache.nifi.spark.NiFiReceiver
import org.apache.nifi.spark.NiFiDataPacket
import org.apache.nifi.remote.client.SiteToSiteClientConfig
import org.apache.nifi.remote.client.SiteToSiteClient
import org.apache.spark.storage.StorageLevel
import java.nio.charset.StandardCharsets

object NifiCassandra {
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

    // Nifi Config    
    val nifiUrl = "127.0.0.1:6969/nifi"
    val nifiSparkStreamingPortName = "NiFi Spark Streaming Port"
    val nifiConfig = new SiteToSiteClient.Builder()
      .url(nifiUrl)
      .portName(nifiSparkStreamingPortName)
      .buildConfig();

    val ratingsStream = ssc.receiverStream(new NiFiReceiver(nifiConfig, StorageLevel.MEMORY_ONLY_2))

    ratingsStream.foreachRDD {
      (message: RDD[(NiFiDataPacket)], batchTime: Time) => {
        message.cache()

        val messageContent = message.map(packet => new String(packet.getContent(), StandardCharsets.UTF_8))

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = messageContent.map(_.split(","))

	// convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token => Rating(token(0).trim.toInt,token(1).trim.toInt,token(2).trim.toInt,batchTime.milliseconds))

        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through spark-env.sh
        //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
        val ratingsDF = ratings.toDF("userid", "itemid", "rating", "timestamp")

        ratingsDF.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(cassandraConfig)
          .save()
	}

	message.unpersist()
   }

    ssc.start()
    ssc.awaitTermination()
  }
}
