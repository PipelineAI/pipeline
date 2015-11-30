package com.advancedspark.core.cache

object CacheNaiveMatrixMultiply {
  def main(args : Array[String]) {
    var dim = args(0).toInt
    var numIters = args(1).toInt
    var mat1 = Array.ofDim[Int](dim,dim)
    var mat2 = Array.ofDim[Int](dim,dim)
    var matResult = Array.ofDim[Int](dim,dim)

    // build matrix 1
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat1(i)(j) = j;

    // built matrix 2
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat2(i)(j) = j;

    // multiply matrix 1 by matrix 2 for numIters
    for (count <- 1 to numIters)
      for (i <- 0 to (dim-1))
        for (j <- 0 to (dim-1))
          for (k <- 0 to (dim-1))
            matResult(i)(j) += mat1(i)(k) * mat2(k)(j);
  }
}
