package com.advancedspark.ml

import com.twitter.algebird.MinHasher
import com.twitter.algebird.MinHasher32
import com.twitter.algebird.MinHashSignature
import org.jblas.DoubleMatrix

object Similarity {
  def cosineSimilarity(vector1: DoubleMatrix, vector2: DoubleMatrix): Double = {
    vector1.dot(vector2) / (vector1.norm2() * vector2.norm2())
  }

  def jaccardSimilarity[T <: Any](seq1: Seq[T], seq2: Seq[T]): Double = {
    val intersection = seq1 intersect seq2
    val union = (seq1 union seq2).toSet.toSeq

    val intersectionSize = intersection.size
    val unionSize = union.size
    val jaccardSimilarity =
      if (unionSize > 0) intersectionSize.toDouble / unionSize.toDouble
      else 0.0

    jaccardSimilarity
  }
}
