package com.advancedspark.ml.graph

import org.apache.spark.graphx._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import org.apache.spark.sql.functions._

case class Item(id: Long, title: String, tags: Seq[String]) {
  override def toString: String = id + ", " + title + ", " + tags
}

object SimilarityPathways {
  def getJaccardSimilarity(item1: Item, item2: Item): Double = {
    val intersectTags = item1.tags intersect item2.tags
    val unionTags = item1.tags union item2.tags

    val numIntersectTags = intersectTags.size
    val numUnionTags = unionTags.size
    val jaccardSimilarity =
      if (numUnionTags > 0) numIntersectTags.toDouble / numUnionTags.toDouble
      else 0.0

    jaccardSimilarity
  }

  def dijkstra(graph: Graph[VertexId, Double], src: VertexId, dest: VertexId) = {
    // Dijkstra Shortest Path
    var graph2 = graph.mapVertices((vid,vd) =>
      (false, if (vid == src) 0 else Double.MaxValue, List[VertexId]()))

    for (i <- 1L to graph.vertices.count-1) {
      // The fold() below simulates minBy() functionality
      val currentVertexId = graph2.vertices.filter(!_._2._1)
        .fold((0L,(false,Double.MaxValue,List[VertexId]())))((a,b) =>
          if (a._2._2 < b._2._2) a else b)._1

      val newDistances = graph2.aggregateMessages[(Double,List[VertexId])](ctx =>
        if (ctx.srcId == currentVertexId)
          ctx.sendToDst((ctx.srcAttr._2 + ctx.attr, ctx.srcAttr._3 :+ ctx.srcId)),
            (a,b) => if (a._1 < b._1) a else b)

      graph2 = graph2.outerJoinVertices(newDistances)((vid, vd, newSum) => {
        val newSumVal = newSum.getOrElse((Double.MaxValue,List[VertexId]()))
          (vd._1 || vid == currentVertexId, math.min(vd._2, newSumVal._1),
            if (vd._2 < newSumVal._1) vd._3 else newSumVal._2)})
    }

    val shortestPathGraph = graph.outerJoinVertices(graph2.vertices)((vid, vd, dist) =>
      (vd, dist.getOrElse((false,Double.MaxValue,List[VertexId]()))
      .productIterator.toList.tail))

    shortestPathGraph

    //val shortestPath = shortestPathGraph.vertices.filter(_._1 == dest)

    //shortestPath
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()

    val sc = SparkContext.getOrCreate(conf)

    val sqlContext = SQLContext.getOrCreate(sc)
    import sqlContext.implicits._

    val itemsDF = sqlContext.read.format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load("file:/root/pipeline/datasets/movielens/ml-latest/movies-sm.csv").toDF("id", "title", "tags")

    // Convert from RDD[Row] to RDD[Item]
    val itemsRDD = itemsDF.select($"id", $"title", $"tags").map(row => {
      val id = row.getInt(0)
      val title = row.getString(1)
      val tags = row.getString(2).trim.split("\\|")
      Item(id, title, tags)
    })

    itemsRDD.collect()

    val allItemPairsRDD = itemsRDD.cartesian(itemsRDD)

    // Filter out duplicates and preserve only the pairs with left id < right id
    //val distinctItemPairsRDD = allItemPairsRDD.filter(itemPair => itemPair._1.id < itemPair._2.id)

    val minJaccardSimilarityThreshold = 0.0001

    // Calculate Jaccard Similarity between all item pairs (cartesian, then de-duped)
    // Only keep pairs with a Jaccard Similarity above a specific threshold
    val similarItemsAboveThresholdRDD = allItemPairsRDD.flatMap(itemPair => {
      val jaccardSim = getJaccardSimilarity(itemPair._1, itemPair._2)
      if (jaccardSim >= minJaccardSimilarityThreshold)
        Some(itemPair._1.id.toLong, itemPair._2.id.toLong, jaccardSim.toDouble)
      else
        None
    })

    similarItemsAboveThresholdRDD.collect().mkString(",")

    val similarItemsAboveThresholdEdgeRDD = similarItemsAboveThresholdRDD.map(rdd => {
      Edge(rdd._1.toLong, rdd._2.toLong, rdd._3.toDouble) 
    })

    val mygraph = Graph.fromEdges(similarItemsAboveThresholdEdgeRDD, 0L)

    val src = 1 
    val dest = 9

    val shortestPathGraph = dijkstra(mygraph, src, dest)

    val shortestPath = shortestPathGraph.vertices.filter(_._1 == dest).map(_._2).collect()
    println("ShortestPath: " + shortestPath)
  }
}
