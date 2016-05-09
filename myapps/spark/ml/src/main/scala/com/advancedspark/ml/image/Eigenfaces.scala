package com.advancedspark.ml.image

import java.util.Properties
import scala.collection.JavaConversions._
import org.apache.spark.sql.Row
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import java.io.File
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.Matrix
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.mllib.feature.StandardScaler
import breeze.linalg.DenseMatrix
import breeze.linalg.csvwrite

object Eigenfaces {
  def saveMatrix(matrix: DenseMatrix[Double], filename: String): Unit = {
    val file = new File(filename)
    file.getParentFile().mkdirs()
    csvwrite(file, matrix)
  }

  def extractPixelArrays(imagePath: String, width: Int, height: Int): Array[Double] = {
    import java.awt.image.BufferedImage
    import javax.imageio.ImageIO 

    val originalImage = ImageIO.read(new File(imagePath))
    
    val newImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    val graphics = newImage.getGraphics()
    graphics.drawImage(originalImage, 0, 0, width, height, null)
    graphics.dispose()
    
    newImage.getData.getPixels(0, 0, width, height, Array.ofDim[Double](width * height))
  }

  /** 
    val inputImagesPath = args[0]
    val outputCsvPath = args[1]
    val scaledWidth = args[2]
    val scaledHeight = args[3]
    val numPrincipalComponents = args[4]
  */
  def main(args: Array[String]) {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)

    val inputImagesPath = args(0)
    val outputCsvPath = args(1)
    val scaledWidth = args(2).toInt
    val scaledHeight = args(3).toInt
    val principalComponents = args(4).toInt

    val imageFilesRDD = sc.wholeTextFiles(inputImagesPath).map {
      case (filename, content) => filename.replace("file:", "")
    }

    val imagesAsPixelArrays = imageFilesRDD.map(imageFile => extractPixelArrays(imageFile, scaledWidth, scaledHeight))

    val imagesAsPixelVectors = imagesAsPixelArrays.map(pixelArray => Vectors.dense(pixelArray))

    // Fit the standard scaler transformer
    val standardScaler = new StandardScaler(withMean = true, withStd = false)
      .fit(imagesAsPixelVectors)

    // Substract mean to normalize the pixel data
    val scaledImagesAsPixelVectors = imagesAsPixelVectors.map(standardScaler.transform(_))

    // Create RowMatrix out of RDD[Vector]
    val scaledImagesAsPixelsVectorsMatrix = new RowMatrix(scaledImagesAsPixelVectors)

    // Find Principal Components to reveal the underlying structure of the data
    val principalComponentsMatrix = scaledImagesAsPixelsVectorsMatrix.computePrincipalComponents(principalComponents)
    val (numPixels, numPrincipalComponents) = (principalComponentsMatrix.numRows, principalComponentsMatrix.numCols)
    
    // Convert to Breeze Matrix to take advantage of Breeze's CSV-export utility
    val principalComponentsBreezeMatrix = new DenseMatrix(numPixels, numPrincipalComponents, principalComponentsMatrix.toArray)
    saveMatrix(principalComponentsBreezeMatrix, s"""$outputCsvPath/principal-components.csv""")

    val scaledImagesProjectedIntoPrincipalComponentSpaceMatrix = scaledImagesAsPixelsVectorsMatrix.multiply(principalComponentsMatrix)

    val (numImages, numProjectedFeatures) =
      (scaledImagesProjectedIntoPrincipalComponentSpaceMatrix.numRows, scaledImagesProjectedIntoPrincipalComponentSpaceMatrix.numCols)
  }
}
