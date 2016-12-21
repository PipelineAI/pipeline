package com.advancedspark.spark.ml

import org.apache.spark.mllib.linalg.Matrix
import java.io.File
import breeze.linalg.DenseMatrix
import breeze.linalg.csvwrite

object MatrixIO {
  def saveMatrix(matrix: Matrix, filename: String): Unit = {
    // Convert to Breeze DenseMatrix to take advantage of Breeze's CSV-export utility
    val breezeMatrix = new DenseMatrix(matrix.numRows, matrix.numCols, matrix.toArray)

    val file = new File(filename)
    file.getParentFile().mkdirs()
    csvwrite(file, breezeMatrix)
  }

  def saveArray(array: Array[Double], filename: String): Unit = {
    // Convert to Breeze DenseMatrix to take advantage of Breeze's CSV-export utility
    val breezeMatrix = new DenseMatrix(1, array.length, array)

    val file = new File(filename)
    file.getParentFile().mkdirs()
    csvwrite(file, breezeMatrix)
  }
}
