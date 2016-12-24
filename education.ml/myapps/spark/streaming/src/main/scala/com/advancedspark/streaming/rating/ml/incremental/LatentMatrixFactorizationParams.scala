package com.advancedspark.streaming.rating.ml.incremental

import org.apache.spark.storage.StorageLevel

/**
 * Parameters for training a Matrix Factorization Model
 */
class LatentMatrixFactorizationParams() {
  private var rank: Int = 20
  private var minRating: Float = 1f
  private var maxRating: Float = 5f
  private var stepSize: Double = 1.0
  private var biasStepSize: Double = 1.0
  private var stepDecay: Double = 0.9
  private var lambda: Double = 10.0
  private var iter: Int = 10
  private var intermediateStorageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER
  private var seed: Long = System.currentTimeMillis()

  def getRank: Int = rank
  def getMinRating: Float = minRating
  def getMaxRating: Float = maxRating
  def getStepSize: Double = stepSize
  def getBiasStepSize: Double = biasStepSize
  def getStepDecay: Double = stepDecay
  def getLambda: Double = lambda
  def getIter: Int = iter
  def getIntermediateStorageLevel: StorageLevel = intermediateStorageLevel
  def getSeed: Long = seed

  /** The rank of the matrices. Default = 20 */
  def setRank(x: Int): this.type = {
    rank = x
    this
  }
  /** The minimum allowed rating. Default = 1.0 */
  def setMinRating(x: Float): this.type = {
    minRating = x
    this
  }
  /** The maximum allowed rating. Default = 5.0 */
  def setMaxRating(x: Float): this.type = {
    maxRating = x
    this
  }
  /** The step size to use during Gradient Descent. Default = 0.001 */
  def setStepSize(x: Double): this.type = {
    stepSize = x
    this
  }
  /** The step size to use for bias vectors during Gradient Descent. Default = 0.0001 */
  def setBiasStepSize(x: Double): this.type = {
    biasStepSize = x
    this
  }
  /** The value to decay the step size after each iteration. Default = 0.95 */
  def setStepDecay(x: Double): this.type = {
    stepDecay = x
    this
  }
  /** The regularization parameter. Default = 0.1 */
  def setLambda(x: Double): this.type = {
    lambda = x
    this
  }
  /** The number of iterations for Gradient Descent. Default = 5 */
  def setIter(x: Int): this.type = {
    iter = x
    this
  }
  /** The persistence level for intermediate RDDs. Default = MEMORY_AND_DISK_SER */
  def setIntermediateStorageLevel(x: StorageLevel): this.type = {
    intermediateStorageLevel = x
    this
  }

  /** The number of iterations for Gradient Descent. Default = 5 */
  def setSeed(x: Long): this.type = {
    seed = x
    this
  }
}
