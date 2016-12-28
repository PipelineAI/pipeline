package com.advancedspark.ml.graph

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

object SimilarityPathway {
  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val itemsDF = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("file:/root/pipeline/datasets/movielens/ml-latest/movies-sm.csv")
      .toDF("id", "title", "tags")

    // Convert from RDD[Row] to RDD[Item]
    val itemsRDD = itemsDF.select($"id", $"title", $"tags").map(row => {
      val id = row.getInt(0)
      val title = row.getString(1)
      val tags = row.getString(2).trim.split("\\|")
      TaggedItem(id, title, tags)
    })

    val allItemPairsRDD = itemsRDD.cartesian(itemsRDD)

    val minJaccardSimilarityThreshold = 0.01

    // Calculate Jaccard Similarity between all item pairs (cartesian, then de-duped)
    // Only keep pairs with a Jaccard Similarity above a specific threshold
    val similarItemsAboveThresholdRDD = allItemPairsRDD.flatMap(itemPair => {
      val jaccardSim = Similarity.jaccardSimilarity(itemPair._1.tags, itemPair._2.tags)
      if (jaccardSim >= minJaccardSimilarityThreshold)
        Some(itemPair._1.id.toLong, itemPair._2.id.toLong, jaccardSim.toDouble)
      else
        None
    })

    similarItemsAboveThresholdRDD.collect().mkString(",")

    val similarItemsAboveThresholdEdgeRDD = similarItemsAboveThresholdRDD.map(rdd => {
      Edge(rdd._1.toLong, rdd._2.toLong, rdd._3.toDouble)
    })

    val graph = Graph.fromEdges(similarItemsAboveThresholdEdgeRDD, 0L)

    val src = 1
    val dest = 9

    val heaviestPathGraph = Dijkstra.heaviestPath(graph, src)

    val heaviestPath = heaviestPathGraph.vertices.filter(_._1 == dest).map(_._2).collect()(0)._2
    println("Heaviest Path: " + heaviestPath)
  }
}
