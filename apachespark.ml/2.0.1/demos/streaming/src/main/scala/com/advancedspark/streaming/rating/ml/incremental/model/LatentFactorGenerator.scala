package com.advancedspark.streaming.rating.ml.incremental.model

import java.util.Random

class LatentFactorGenerator(rank: Int, min: Float, max: Float) extends Serializable {
  private val random = new Random()
  private val scale = max - min

  def nextValue(): LatentFactor = {
    new LatentFactor(scaleValue(random.nextDouble),
      Array.tabulate(rank)(i => scaleValue(random.nextDouble)))
  }

  def scaleValue(value: Double): Float = math.sqrt(value * scale + min).toFloat / rank
  
  def setSeed(seed: Long) = random.setSeed(seed)
}
