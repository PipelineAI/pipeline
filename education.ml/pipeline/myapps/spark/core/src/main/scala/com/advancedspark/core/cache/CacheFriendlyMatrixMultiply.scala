package com.advancedspark.core.cache

object CacheFriendlyMatrixMultiply {
  def main(args : Array[String]) {
    val dim = args(0).toInt
    val numIters = args(1).toInt
    var mat1 = Array.ofDim[Int](dim,dim)
    var mat2transpose = Array.ofDim[Int](dim,dim)
    var matResult = Array.ofDim[Int](dim,dim)

    // Build matrix 1
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat1(i)(j) = j;

    // Build matrix 2 transpose
    // Note: We're not actually doing a transpose here as this would
    //         throw off the comparison to the CacheNaiveMatrixMultiply
    //         in terms of overall performance.
    //       We are, however,         
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat2transpose(i)(j) = j;

    // Multiply matrix 1 by matrix 2 transpose for numIters
    // Note:  We are performing this algorithm as if matrix 2 
    //          was transposed as this is what we are comparing 
    for (count <- 1 to numIters)
      for (i <- 0 to (dim-1))
        for (j <- 0 to (dim-1))
          for (k <- 0 to (dim-1))
            matResult(i)(j) += mat1(i)(k) * mat2transpose(j)(k);
  }
}
