package com.advancedspark.ml.nlp

import org.apache.spark.sql.types._
import org.apache.spark.sql.Row
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._

import com.databricks.spark.corenlp.CoreNLP

object ItemDescriptionsDF {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val input = sqlContext.createDataFrame(Seq(
      (1, "<xml>Stanford University is located in California. It is a great university.</xml>")
    )).toDF("id", "text")
    
    val coreNLP = new CoreNLP()
      .setInputCol("text")
      .setAnnotators(Array("tokenize", "cleanxml", "ssplit"))
      .setFlattenNestedFields(Array("sentence_token_word", "sentence_characterOffsetBegin"))
      .setOutputCol("parsed")
   
    val parsed = coreNLP.transform(input)
      .select("parsed.sentence_token_word", "parsed.sentence_characterOffsetBegin")
    
    println(parsed.first())
  }
}
