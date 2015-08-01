import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkContext
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SaveMode
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.Time

case class Rating(fromUserId: Int, toUserId: Int, rating: Int)

object StreamingRatings {
  def main(args:  Array[String]) {
    val conf = new SparkConf()
      .set("spark.cassandra.connection.host", "127.0.0.1")
      .set("spark.cassandra.connection.rpc.port", "9160")
      .set("spark.cassandra.connection.native.port", "9042")
    
    val sc = new SparkContext(conf)

    def create(): StreamingContext = {
      @transient val newSsc = new StreamingContext(sc, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }

    val ssc = StreamingContext.getActiveOrCreate(create)

    val brokers = "localhost:9092,localhost:9093"
    val topics = Set("ratings")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)

    val ratingsStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)

    ratingsStream.foreachRDD {
      (message: RDD[String], batchTime: Time) => {
        // convert each RDD from the batch into a DataFrame
        val df = rdd.map(_.split(",")).map(rating => Rating(rating(0).trim.toInt, rating(1).trim.toInt, rating(2).trim.toInt)).toDF()
	//"fromuserid", "touserid", "rating")
      
        // add the batch time to the DataFrame
        val dfWithBatchTime = df.withColumn("batch_time", lit(batchTime.milliseconds))
      
        // save the DataFrame to Cassandra
        // Note:  Cassandra has been initialized through spark-env.sh
        //        Specifically, export SPARK_JAVA_OPTS=-Dspark.cassandra.connection.host=127.0.0.1
        dfWithBatchTime.write.format("org.apache.spark.sql.cassandra")
          .mode(SaveMode.Append)
          .options(Map("keyspace" -> "fluxcapacitor", "table" -> "real_time_ratings"))
          .save()
      }
    }
  }
}
