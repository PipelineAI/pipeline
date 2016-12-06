package com.advancedspark.streaming.rating.ml

import org.jblas.DoubleMatrix

object Similarity {
  def cosineSimilarity(matrix1: DoubleMatrix, matrix2: DoubleMatrix): Double = {
    matrix1.dot(matrix2) / (matrix1.norm2() * matrix2.norm2())
  }

  //def cosineSimilarity(vector1: Vector, vector2: Vector): Double = {
  //  BLAS.dot(vector1, vector2) / (Vectors.norm(vector1, 2) * Vectors.norm(vector2, 2))
  //}

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
