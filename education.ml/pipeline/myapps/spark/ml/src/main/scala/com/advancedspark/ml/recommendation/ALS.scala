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
    //sqlContext.setConf("spark.sql.shuffle.partitions", "500")

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
      .load("file:/root/pipeline/datasets/movielens/ml-latest/ratings-sm.csv")
      .toDF("userId", "itemId", "rating", "timestamp")
      .as("itemRatings")

    import org.apache.spark.ml.recommendation.ALS

    val rank = 5 // this is k, number of latent factors we think exist
    val maxIterations = 10
    val convergenceThreshold = 0.01
    val implicitPrefs = false
    val alpha = 1.0
    val nonnegative = true

    val als = new ALS()
      .setRank(rank)
      .setMaxIter(maxIterations)
      .setRegParam(convergenceThreshold)
      .setImplicitPrefs(implicitPrefs)
      .setAlpha(alpha)
      .setNonnegative(nonnegative)
      .setUserCol("userId")
      .setItemCol("itemId")
      .setRatingCol("rating")
      .setPredictionCol("prediction")

    val allDistinctUsersDF = itemRatingsDF.select($"userId").dropDuplicates()

    val allDistinctUserItemPairsDF = allDistinctUsersDF
      .join(itemsDF.select($"itemId"))
      .select($"userId", $"itemId")
      .distinct()

    import org.apache.spark.ml.recommendation.ALSModel
    
    val model = als.fit(itemRatingsDF)

    val recommendationsDF = model.transform(allDistinctUserItemPairsDF)
      .as("recommendations")
      .cache()
  
    val enrichedRecommendationsDF =
      recommendationsDF.join(itemsDF, $"items.itemId" === $"recommendations.itemId")
      .select($"userId", $"recommendations.itemId", $"title", $"tags", $"prediction", $"tags")
      .sort($"userId", $"prediction" desc, $"recommendations.itemId")
      .na.drop(Seq("prediction"))

    System.out.println(enrichedRecommendationsDF.head(10).mkString("\n"))
  }
}
