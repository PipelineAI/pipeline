package com.advancedspark.streaming.recommendation.optimization

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import com.advancedspark.streaming.recommendation._
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD

/**
 * A Gradient Descent Optimizer specialized for Matrix Factorization.
 *
 * @param params The parameters to use
 */
class MFGradientDescent(params: LatentMatrixFactorizationParams) {

  def this() = this(new LatentMatrixFactorizationParams)

  def train(
      ratings: RDD[Rating[Long]],
      initialModel: LatentMatrixFactorizationModel,
      numExamples: Long): LatentMatrixFactorizationModel = {

    var userFeatures = initialModel.userFeatures
    var prodFeatures = initialModel.productFeatures
    val globalBias = initialModel.globalBias
    val lambda = params.getLambda
    val stepSize = params.getStepSize
    val stepDecay = params.getStepDecay
    val biasStepSize = params.getBiasStepSize
    val iter = params.getIter
    val intermediateStorageLevel = params.getIntermediateStorageLevel
    val rank = params.getRank

    for (i <- 0 until iter) {
      val currentStepSize = stepSize * math.pow(stepDecay, i)
      val currentBiasStepSize = biasStepSize * math.pow(stepDecay, i)
      val gradients = ratings.map(r => (r.user, r)).join(userFeatures)
        .map { case (user, (rating, uFeatures)) =>
          (rating.item, (user, rating.rating, uFeatures))
        }.join(prodFeatures).map { case (item, ((user, rating, uFeatures), pFeatures)) =>
          val step = MFGradientDescent.gradientStep(rating, uFeatures, pFeatures,
            globalBias, currentStepSize, currentBiasStepSize, lambda)
          ((user, step._1), (item, step._2))
        }.persist(intermediateStorageLevel)
      val userGradients = IndexedRDD(gradients.map(_._1)
        .aggregateByKey(new LatentFactor(0f, new Array[Float](rank)))(
          seqOp = (base, example) => base += example,
          combOp = (a, b) => a += b
        ))
      val prodGradients = IndexedRDD(gradients.map(_._2)
        .aggregateByKey(new LatentFactor(0f, new Array[Float](rank)))(
          seqOp = (base, example) => base += example,
          combOp = (a, b) => a += b
        ))
      userFeatures = userFeatures.leftJoin(userGradients) { case (id, base, gradient) =>
        gradient.foreach(g => base.divideAndAdd(g, numExamples))
        base
      }
      prodFeatures = prodFeatures.leftJoin(prodGradients) { case (id, base, gradient) =>
        gradient.foreach(g => base.divideAndAdd(g, numExamples))
        base
      }
    }
    initialModel match {
      case streaming: StreamingLatentMatrixFactorizationModel =>
        new StreamingLatentMatrixFactorizationModel(rank, userFeatures, prodFeatures,
          globalBias, streaming.observedExamples, initialModel.minRating, initialModel.maxRating)
      case _ =>
        new LatentMatrixFactorizationModel(rank, userFeatures, prodFeatures, globalBias,
          initialModel.minRating, initialModel.maxRating)
    }
  }
}

object MFGradientDescent extends Serializable {

  // Exposed for testing
  def gradientStep(
      rating: Float,
      userFeatures: LatentFactor,
      prodFeatures: LatentFactor,
      bias: Float,
      stepSize: Double,
      biasStepSize: Double,
      lambda: Double): (LatentFactor, LatentFactor) = {
    val predicted = LatentMatrixFactorizationModel.getRating(userFeatures, prodFeatures, bias)
    val epsilon = rating - predicted
    val user = userFeatures.vector
    val rank = user.length
    val prod = prodFeatures.vector

    val featureGradients = Array.tabulate(rank) { i =>
      ((stepSize * (prod(i) * epsilon - lambda * user(i))).toFloat,
        (stepSize * (user(i) * epsilon - lambda * prod(i))).toFloat)
    }
    val userBiasGrad: Float = (biasStepSize * (epsilon - lambda * userFeatures.bias)).toFloat
    val prodBiasGrad: Float = (biasStepSize * (epsilon - lambda * prodFeatures.bias)).toFloat

    val uFeatures = featureGradients.map(_._1)
    val pFeatures = featureGradients.map(_._2)
    (new LatentFactor(userBiasGrad, uFeatures), new LatentFactor(prodBiasGrad, pFeatures))
  }
}
