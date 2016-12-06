package com.advancedspark.streaming.rating.ml.incremental.utils

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

object VectorUtils extends Serializable {

  def dot(a: Array[Float], b: Array[Float]): Float = {
    require(a.length == b.length, "Trying to dot product vectors with different lengths.")
    var sum = 0f
    val len = a.length
    var i = 0
    while (i < len) {
      sum += a(i) * b(i)
      i += 1
    }
    sum
  }

  def addInto(into: Array[Float], x: Array[Float], scale: Long = 1L): Unit = {
    require(into.length == x.length, "Trying to add vectors with different lengths.")
    val len = into.length
    var i = 0
    while (i < len) {
      into(i) += x(i) / scale
      i += 1
    }
  }

}
