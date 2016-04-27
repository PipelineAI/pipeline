package com.advancedspark.streaming.recommendation.model

import com.advancedspark.streaming.recommendation.utils.VectorUtils

case class LatentFactor(var bias: Float, vector: Array[Float]) extends Serializable {

  def +=(other: LatentFactor): this.type = {
    bias += other.bias
    VectorUtils.addInto(vector, other.vector)
    this
  }

  def divideAndAdd(other: LatentFactor, scale: Long): this.type = {
    bias += other.bias / scale
    VectorUtils.addInto(vector, other.vector, scale)
    this
  }

  override def toString: String = {
    s"bias: $bias, factors: " + vector.mkString(", ")
  }
}
