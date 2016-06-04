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
      .load("file:/root/pipeline/datasets/movielens/ml-latest/ratings-sm.csv")
      .toDF("userId", "itemId", "rating", "timestamp")
      .as("itemRatings")

    val Array(trainingItemRatingsDF, testItemRatingsDF) = itemRatingsDF.randomSplit(Array(0.8, 0.2))

    import org.apache.spark.ml.recommendation.ALS

    val rank = 10 // this is k, number of latent factors we think exist
    val maxIterations = 20
    val convergenceThreshold = 0.01
    val implicitPrefs = true
    val alpha = 1.0

    val als = new ALS()
      .setRank(rank)
      .setRegParam(convergenceThreshold)
      .setImplicitPrefs(implicitPrefs)
      .setAlpha(alpha)
      .setUserCol("userId")
      .setItemCol("itemId")
      .setRatingCol("rating")

    val trainingModel = als.fit(trainingItemRatingsDF)

    trainingModel.setPredictionCol("prediction")

    import org.apache.spark.ml.evaluation.RegressionEvaluator

    val actualItemRatingsDF = trainingModel.transform(testItemRatingsDF)

    val modelEvaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")

    val rmse = modelEvaluator.evaluate(actualItemRatingsDF)

    System.out.println(s"Root Mean Square Error = $rmse between actual and expected itemRatings")

    // Retrain with all data
    val model = als.fit(itemRatingsDF)

    model.setPredictionCol("prediction")

    val allDistinctUsersDF = itemRatingsDF
      .select($"userId")
      .distinct()
      .as("users")

    val allDistinctItemsDF = itemRatingsDF
      .select($"itemId")
      .distinct()
      .as("items")

    val allUserItemPairsDF = allDistinctUsersDF.join(allDistinctItemsDF)
      .except(itemRatingsDF.select($"userId", $"itemId"))

    val recommendationsDF = model.transform(allUserItemPairsDF)
      .as("recommendations")
      .cache()

    val enrichedRecommendationsDF =
      recommendationsDF.join(itemsDF, $"items.itemId" === $"recommendations.itemId")
      .select($"userId", $"recommendations.itemId", $"title", $"tags", $"prediction", $"tags")
      .sort($"userId", $"prediction" desc, $"recommendations.itemId")

    System.out.println(enrichedRecommendationsDF.take(50))
  }
}
