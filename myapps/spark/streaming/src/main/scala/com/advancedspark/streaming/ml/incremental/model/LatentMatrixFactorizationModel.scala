package com.advancedspark.streaming.ml.incremental.model

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

import org.apache.spark.Logging
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD

class LatentMatrixFactorizationModel(
    val rank: Int,
    val userFeatures: IndexedRDD[Long, LatentFactor], // bias and the user row
    val productFeatures: IndexedRDD[Long, LatentFactor], // bias and the product row
    val globalBias: Float,
    val minRating: Float,
    val maxRating: Float) extends Logging {

  /** Predict the rating of one user for one product. */
  def predict(user: Long, product: Long): Float = {
    val uFeatures = userFeatures.get(user)
    val pFeatures = productFeatures.get(product)
    LatentMatrixFactorizationModelOps.predict(user, product, uFeatures, pFeatures, globalBias,
      minRating, maxRating).rating
  }

  /**
   * Predict the rating of many users for many products.
   * The output RDD will return a prediction for all user - product pairs. For users or
   * products that were missing from the training data, the prediction will be made with the global
   * bias (global average) +- the user or product bias, if they exist.
   *
   * @param usersProducts  RDD of (user, product) pairs.
   * @return RDD of Ratings.
   */
  def predict(usersProducts: RDD[(Long, Long)]): RDD[Rating[Long]] = {
    val users = usersProducts.leftOuterJoin(userFeatures).map { case (user, (product, uFeatures)) =>
      (product, (user, uFeatures))
    }
    val sc = usersProducts.sparkContext
    val globalAvg = sc.broadcast(globalBias)
    val min = sc.broadcast(minRating)
    val max = sc.broadcast(maxRating)
    users.leftOuterJoin(productFeatures).map { case (product, ((user, uFeatures), pFeatures)) =>
      LatentMatrixFactorizationModelOps.predict(user, product, uFeatures, pFeatures, globalAvg.value,
        min.value, max.value)
    }
  }
}
