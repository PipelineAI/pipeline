package com.advancedspark.tungsten.matrix

object CacheFriendlyMatrixMultiply{
  def main(args : Array[String]) {
    val dim = args(0).toInt
    val numIters = args(1).toInt
    var mat1 = Array.ofDim[Int](dim,dim)
    var mat2transpose = Array.ofDim[Int](dim,dim)
    var matResult = Array.ofDim[Int](dim,dim)

    // build matrix 1
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat1(i)(j) = j;

    // build matrix 2 transpose
    for (i <- 0 to (dim-1))
      for ( j <- 0 to (dim-1))
        mat2transpose(i)(j) = j;

    // multiply matrix 1 by matrix 2 transpose for numIters
    for (count <- 1 to numIters)
      for (i <- 0 to (dim-1))
        for (j <- 0 to (dim-1))
          for (k <- 0 to (dim-1))
            matResult(i)(j) += mat1(i)(k) * mat2transpose(j)(k);
  }
}
