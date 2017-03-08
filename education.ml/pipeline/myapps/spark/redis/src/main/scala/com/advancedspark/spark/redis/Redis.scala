package com.advancedspark.spark.redis

import java.util.Properties
import scala.collection.JavaConversions._
import org.apache.spark.sql.Row
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import com.redislabs.provider.redis._

object Redis {
  def main(args: Array[String]) {
    val conf = new SparkConf()
//      .set("redis.host", "127.0.0.1")
//      .set("redis.port", "6379")

    val sc = new SparkContext(conf)

    sc.toRedisKV(sc.parallelize(("key1", "val1") :: Nil))
    sc.toRedisKV(sc.parallelize(("key2", "val2") :: Nil)) 
    
    val valuesRDD = sc.fromRedisKV("key1") 
    val values = valuesRDD.collect()

    System.out.println("values: " + values(0)._2)
  }
}
