package com.advancedspark.ml.recommendation

import org.apache.spark.graphx._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import org.apache.spark.sql.functions._

import com.advancedspark.ml.TaggedItem
import com.advancedspark.ml.Similarity

object ALS {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val itemsDF = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("file:/root/pipeline/datasets/movielens/ml-latest/movies.csv")
      .toDF("itemId", "title", "tags")
      .as("items")

    val itemRatingsDF = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("file:/root/pipeline/datasets/movielens/ml-latest/ratings.csv")
      .toDF("userId", "itemId", "rating", "timestamp")
      .as("itemRatings")

    import org.apache.spark.ml.recommendation.ALS

    val rank = 10 // this is k, number of latent factors we think exist
    val maxIterations = 20
    val convergenceThreshold = 0.01

    val als = new ALS()
      .setRank(rank)
      .setRegParam(convergenceThreshold)
      .setUserCol("userId")
      .setItemCol("itemId")
      .setRatingCol("rating")

    val model = als.fit(itemRatingsDF)

    model.setPredictionCol("confidence")

    val recommendationsDF = model.transform(itemRatingsDF.select($"userId", $"itemId"))
      .toDF("userId", "recommendedItemId", "confidence")

    val enrichedRecommendationsDF =
      recommendationsDF.join(itemsDF, $"items.itemId" === $"recommendedItemId")
      .select($"userId", $"recommendedItemId", $"title", $"tags", $"confidence", $"tags")
      .sort($"userId", $"recommendedItemId", $"confidence" desc)

    System.out.println(enrichedRecommendationsDF.take(5))
  }
}
