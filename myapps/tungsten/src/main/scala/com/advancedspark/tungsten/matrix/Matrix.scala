package com.advancedspark.tungsten.matrix

object NaiveMatrixMultiply{
  def main(args : Array[String]) {
    var mat1 = Array.ofDim[Int](10000,10000)
    var mat2 = Array.ofDim[Int](10000,10000)
    var matResult = Array.ofDim[Int](10000,10000)

    // build matrix 1
    for (i <- 0 to 9999)
      for ( j <- 0 to 9999)
        mat1(i)(j) = j;

    // built matrix 2
    for (i <- 9999 to 0)
      for ( j <- 9999 to 0)
        mat2(i)(j) = j;

    // multiply matrix 1 by matrix 2 transpose 5 times
    for (count <- 1 to 5)
      for (i <- 0 to 9999)
        for (j <- 0 to 9999)
          for (k <- 0 to 9999)
            matResult(i)(j) += mat1(i)(k) * mat2(k)(j);
  }
}

object CacheAwareMatrixMultiply{
  def main(args : Array[String]) {
    var mat1 = Array.ofDim[Int](10000,10000)
    var mat2 = Array.ofDim[Int](10000,10000)
    var mat2transpose = Array.ofDim[Int](10000,10000)
    var matResult = Array.ofDim[Int](10000,10000)

    // build matrix 1
    for (i <- 0 to 9999)
      for ( j <- 0 to 9999)
        mat1(i)(j) = j;
    
    // build matrix 2
    for (i <- 9999 to 0)
      for ( j <- 9999 to 0)
        mat2(i)(j) = j;

    // transpose matrix 2
    for (i <- 0 to 9999)
      for ( j <- 0 to 9999)
        mat2transpose(i)(j) = mat2(j)(i);

    // multiply matrix 1 by matrix 2 transpose 500 times
    for (count <- 1 to 5)
      for (i <- 0 to 9999)
        for (j <- 0 to 9999)
          for (k <- 0 to 9999)
            matResult(i)(j) += mat1(i)(k) * mat2(j)(k);
  }
} 

