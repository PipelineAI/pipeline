package com.advancedspark.ml.graph

import org.apache.spark.graphx._
import scala.reflect.ClassTag

object Cluster {
  def globalClusteringCoefficient[VD:ClassTag,ED:ClassTag](g:Graph[VD,ED]) = {
    val numTriplets =
      g.aggregateMessages[Set[VertexId]]( et => { 
        et.sendToSrc(Set(et.dstId)); 
        et.sendToDst(Set(et.srcId)) }, 
        (a,b) => a ++ b) 
      .map(x => {val s = (x._2 - x._1).size; s*(s-1) / 2})
      .reduce(_ + _)
    if (numTriplets == 0) 0.0 
    else g.triangleCount.vertices.map(_._2).reduce(_ + _) / numTriplets.toFloat
  }
}
