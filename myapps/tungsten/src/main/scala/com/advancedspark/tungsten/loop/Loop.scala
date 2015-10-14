package com.advancedspark.tungsten.loop

object NaiveLoop {
  def main(args : Array[String]) {
    var mat1 = Array.ofDim[Int](10000,10000)
    var mat2 = Array.ofDim[Int](10000,10000)

    // build matrix 1
    for (r <- 0 to 9999)
      for (c <- 0 to 9999)
        mat1(r)(c) = c;
    
    // build matrix 2
    for (r <- 9999 to 0)
      for (c <- 9999 to 0)
        mat2(r)(c) = mat1(r)(c);
  }
} 

object CacheAwareLoop {
  def main(args : Array[String]) {
    var mat1 = Array.ofDim[Int](10000,10000)
    var mat1transpose = Array.ofDim[Int](10000,10000)

    // build matrix 1
    for (r <- 0 to 9999)
      for (c <- 0 to 9999)
        mat1(r)(c) = c;

    // transpose matrix 1
    for (r <- 0 to 9999)
      for (c <- 0 to 9999)
        mat1transpose(r)(c) = mat1(c)(r);
  }
} 
