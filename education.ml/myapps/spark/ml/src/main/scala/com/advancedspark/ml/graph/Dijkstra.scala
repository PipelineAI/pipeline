package com.advancedspark.ml.graph

import org.apache.spark.graphx._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.Row

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import org.apache.spark.sql.functions._

object Dijkstra {
  def lightestPath(graph: Graph[VertexId, Double], src: VertexId): Graph[(VertexId, List[Any]), Double] = {
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

    // TODO:  Return only the edges that make up the shortest path
    val lightestPathGraph = graph.outerJoinVertices(graph2.vertices)((vid, vd, dist) =>
      (vd, dist.getOrElse((false,Double.MaxValue,List[VertexId]()))
      .productIterator.toList.tail))

    lightestPathGraph
  }

  def heaviestPath(graph: Graph[VertexId, Double], src: VertexId): Graph[(VertexId, List[Any]), Double] = {
    // Dijkstra Shortest Path
    var graph2 = graph.mapVertices((vid,vd) =>
      (false, if (vid == src) 0 else Double.MinValue, List[VertexId]()))

    for (i <- 1L to graph.vertices.count-1) {
      // The fold() below simulates minBy() functionality
      val currentVertexId = graph2.vertices.filter(!_._2._1)
        .fold((0L,(false,Double.MinValue,List[VertexId]())))((a,b) =>
          if (a._2._2 > b._2._2) a else b)._1

      val newDistances = graph2.aggregateMessages[(Double,List[VertexId])](ctx =>
        if (ctx.srcId == currentVertexId)
          ctx.sendToDst((ctx.srcAttr._2 + ctx.attr, ctx.srcAttr._3 :+ ctx.srcId)),
            (a,b) => if (a._1 > b._1) a else b)

      graph2 = graph2.outerJoinVertices(newDistances)((vid, vd, newSum) => {
        val newSumVal = newSum.getOrElse((Double.MinValue,List[VertexId]()))
          (vd._1 || vid == currentVertexId, math.max(vd._2, newSumVal._1),
            if (vd._2 > newSumVal._1) vd._3 else newSumVal._2)})
    }

    // TODO:  Return only the edges that make up the shortest path
    val heaviestPathGraph = graph.outerJoinVertices(graph2.vertices)((vid, vd, dist) =>
      (vd, dist.getOrElse((false,Double.MinValue,List[VertexId]()))
      .productIterator.toList.tail))

    heaviestPathGraph
  }
}

