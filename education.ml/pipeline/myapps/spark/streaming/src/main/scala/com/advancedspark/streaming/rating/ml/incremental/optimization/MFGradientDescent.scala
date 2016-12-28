package com.advancedspark.streaming.rating.ml.incremental.optimization

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import com.advancedspark.streaming.rating.ml.incremental.LatentMatrixFactorizationParams
import com.advancedspark.streaming.rating.ml.incremental.model.StreamingLatentMatrixFactorizationModel
import com.advancedspark.streaming.rating.ml.incremental.model.LatentMatrixFactorizationModel
import com.advancedspark.streaming.rating.ml.incremental.model.LatentMatrixFactorizationModelOps
import com.advancedspark.streaming.rating.ml.incremental.model.LatentFactorGenerator
import com.advancedspark.streaming.rating.ml.incremental.model.LatentFactor
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

    var userFactors = initialModel.userFactors
    var itemFactors = initialModel.itemFactors
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
      val gradients = ratings.map(r => (r.user, r)).join(userFactors)
        .map { case (user, (rating, userFactorsForSingleUser)) =>
          (rating.item, (user, rating.rating, userFactorsForSingleUser))
        }.join(itemFactors).map { case (item, ((user, rating, userFactorsForSingleUser), itemFactorsForSingleUser)) =>
          val step = MFGradientDescent.gradientStep(rating, userFactorsForSingleUser, itemFactorsForSingleUser,
            globalBias, currentStepSize, currentBiasStepSize, lambda)
          ((user, step._1), (item, step._2))
        }.persist(intermediateStorageLevel)
      val userGradients = IndexedRDD(gradients.map(_._1)
        .aggregateByKey(new LatentFactor(0f, new Array[Float](rank)))(
          seqOp = (base, example) => base += example,
          combOp = (a, b) => a += b
        ))
      val itemGradients = IndexedRDD(gradients.map(_._2)
        .aggregateByKey(new LatentFactor(0f, new Array[Float](rank)))(
          seqOp = (base, example) => base += example,
          combOp = (a, b) => a += b
        ))
      userFactors = userFactors.leftJoin(userGradients) { case (id, base, gradient) =>
        gradient.foreach(g => base.divideAndAdd(g, numExamples))
        base
      }
      itemFactors = itemFactors.leftJoin(itemGradients) { case (id, base, gradient) =>
        gradient.foreach(g => base.divideAndAdd(g, numExamples))
        base
      }
    }
    initialModel match {
      case streaming: StreamingLatentMatrixFactorizationModel =>
        new StreamingLatentMatrixFactorizationModel(rank, userFactors, itemFactors,
          globalBias, streaming.observedExamples, initialModel.minRating, initialModel.maxRating)
      case _ =>
        new LatentMatrixFactorizationModel(rank, userFactors, itemFactors, globalBias,
          initialModel.minRating, initialModel.maxRating)
    }
  }
}

object MFGradientDescent extends Serializable {

  // Exposed for testing
  def gradientStep(
      rating: Float,
      userFactors: LatentFactor,
      itemFactors: LatentFactor,
      bias: Float,
      stepSize: Double,
      biasStepSize: Double,
      lambda: Double): (LatentFactor, LatentFactor) = {
    val predicted = LatentMatrixFactorizationModelOps.getRating(userFactors, itemFactors, bias)
    val epsilon = rating - predicted
    val user = userFactors.vector
    val rank = user.length
    val item = itemFactors.vector

    val featureGradients = Array.tabulate(rank) { i =>
      ((stepSize * (item(i) * epsilon - lambda * user(i))).toFloat,
        (stepSize * (user(i) * epsilon - lambda * item(i))).toFloat)
    }
    val userBiasGrad: Float = (biasStepSize * (epsilon - lambda * userFactors.bias)).toFloat
    val itemBiasGrad: Float = (biasStepSize * (epsilon - lambda * itemFactors.bias)).toFloat

    val userFactorsForSingleUser = featureGradients.map(_._1)
    val itemFactorsForSingleItem = featureGradients.map(_._2)
    (new LatentFactor(userBiasGrad, userFactorsForSingleUser), new LatentFactor(itemBiasGrad, itemFactorsForSingleItem))
  }
}
