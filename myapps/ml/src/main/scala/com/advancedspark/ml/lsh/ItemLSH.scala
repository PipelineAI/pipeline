package com.advancedspark.ml.lsh

import com.invincea.spark.hash.{LSH, LSHModel}
import org.apache.spark.mllib.linalg.{Vector, Vectors, SparseVector}
import org.apache.spark.mllib.linalg.{Matrix, Matrices}
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

// Adapted from https://github.com/mrsqueeze/spark-hash

object ItemLSH {
  def main(args: Array[String]) {
    val conf = new SparkConf()
    val sc = SparkContext.getOrCreate(conf)

    val portSet : org.apache.spark.rdd.RDD[(List[Int], Int)] = sc.objectFile("../../datasets/lsh")
    portSet.repartition(8)
    
    val portSetFiltered = portSet.filter(tpl => tpl._1.size > 3)
    val vctr = portSetFiltered.map(r => (r._1.map(i => (i, 1.0)))).map(a => Vectors.sparse(65535, a).asInstanceOf[SparseVector])
    val lsh = new  LSH(data = vctr, p = 65537, m = 1000, numRows = 1000, numBands = 25, minClusterSize = 2)
    val model = lsh.run
    System.out.println(model)
  }  
}
