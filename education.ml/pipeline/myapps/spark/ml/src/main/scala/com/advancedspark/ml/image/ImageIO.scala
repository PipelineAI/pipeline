package com.advancedspark.spark.ml.image

import java.awt.image.BufferedImage
import java.io.File

object ImageIO {
  def extractAndScalePixelArray(imagePath: String, width: Int, height: Int): Array[Double] = {
    val originalImage = javax.imageio.ImageIO.read(new File(imagePath))

    val newImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
  
    val graphics = newImage.getGraphics()
    graphics.drawImage(originalImage, 0, 0, width, height, null)
    graphics.dispose()

    newImage.getData.getPixels(0, 0, width, height, Array.ofDim[Double](width * height))
  }
}
