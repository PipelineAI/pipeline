package com.advancedspark.streaming.rating.ml.incremental.model

import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

import com.advancedspark.streaming.rating.ml.incremental.LatentMatrixFactorizationParams
import com.advancedspark.streaming.rating.ml.incremental.utils.VectorUtils

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD.longSer

// TODO:  Figure out why this is a separate class 
object LatentMatrixFactorizationModelOps extends Serializable {

  /**
   * Adds random factors for missing user - item entries and updates the global bias and
   * number of observed examples. Returns the initialized model, and number of examples in this rdd.
   */
  def train(
      ratings: RDD[Rating[Long]],
      params: LatentMatrixFactorizationParams,
      initialModel: Option[LatentMatrixFactorizationModel],
      isStreaming: Boolean = false): (LatentMatrixFactorizationModel, Long) = {
    val rank = params.getRank
    val minRating = params.getMinRating
    val maxRating = params.getMaxRating
    val seed = params.getSeed

    val randGenerator =
      new LatentFactorGenerator(rank, minRating, maxRating)
     
    // Generate random entries for missing user-item factors
    val usersAndRatings = ratings.map(r => (r.user, r))
    val itemsAndRatings = ratings.map(r => (r.item, r))
    val sc = ratings.sparkContext
    var userFactors = initialModel match {
      case Some(model) => model.userFactors
      case None =>
        IndexedRDD(sc.parallelize(Seq.empty[(Long, LatentFactor)], ratings.partitions.length))
    }

    var itemFactors = initialModel match {
      case Some(model) => model.itemFactors
      case None =>
        IndexedRDD(sc.parallelize(Seq.empty[(Long, LatentFactor)], ratings.partitions.length))
    }

    userFactors = IndexedRDD(usersAndRatings.fullOuterJoin(userFactors)
      .mapPartitionsWithIndex { case (partitionId, iterator) =>
        randGenerator.setSeed(seed + 2 << 16 + partitionId)
        iterator.map { case (user, (rating, userFactors)) =>
          (user, userFactors.getOrElse(randGenerator.nextValue()))
        }
    })

    itemFactors = IndexedRDD(itemsAndRatings.fullOuterJoin(itemFactors)
      .mapPartitionsWithIndex { case (partitionId, iterator) =>
        randGenerator.setSeed(seed + 2 << 32 + partitionId)
        iterator.map { case (user, (rating, itemFactors)) =>
          (user, itemFactors.getOrElse(randGenerator.nextValue()))
        }
    })

    val (ratingSum, numRatings) =
      ratings.map(r => (r.rating, 1L)).reduce((a, b) => (a._1 + b._1, a._2 + b._2))

    val (globalBias, numExamples) = initialModel.getOrElse(None) match {
      case streaming: StreamingLatentMatrixFactorizationModel =>
        val examples: Long = streaming.observedExamples + numRatings
        ((streaming.globalBias * streaming.observedExamples + ratingSum) / examples, examples)
      case _ => (ratingSum / numRatings, numRatings)
    }

    val initializedModel = initialModel.getOrElse(None) match {
      case streaming: StreamingLatentMatrixFactorizationModel =>
        new StreamingLatentMatrixFactorizationModel(rank, userFactors, itemFactors,
          streaming.globalBias, streaming.observedExamples, minRating, maxRating)
      case _ =>
        if (isStreaming) {
          new StreamingLatentMatrixFactorizationModel(rank, userFactors, itemFactors,
            globalBias, numExamples, minRating, maxRating)
        } else {
          new LatentMatrixFactorizationModel(rank, userFactors, itemFactors, globalBias,
            minRating, maxRating)
        }
    }
    (initializedModel, numRatings)
  }

  def predict(
      userId: Long,
      itemId: Long,
      userFactors: Option[LatentFactor],
      itemFactors: Option[LatentFactor],
      globalBias: Float,
      minRating: Float,
      maxRating: Float): Rating[Long] = {
    val finalRating =
      if (userFactors.isDefined && itemFactors.isDefined) {
        Rating(userId, itemId, LatentMatrixFactorizationModelOps.getRating(userFactors.get, itemFactors.get,
          globalBias, minRating, maxRating))
      } else if (userFactors.isDefined) {
        //logWarning(s"Item data missing for item id $itemId. Will use user factors.")
        val rating = globalBias + userFactors.get.bias
        Rating(userId, itemId, math.min(maxRating, math.max(minRating, rating)))
      } else if (itemFactors.isDefined) {
        //logWarning(s"User data missing for user id $userId. Will use item factors.")
        val rating = globalBias + itemFactors.get.bias
        Rating(userId, itemId, math.min(maxRating, math.max(minRating, rating)))
      } else {
        //logWarning(s"Both user and item factors missing for ($userId, $itemId). " +
        //  "Returning global average.")
        val rating = globalBias
        Rating(userId, itemId, math.min(maxRating, math.max(minRating, rating)))
      }
    finalRating
  }

  def getRating(
      userFactors: LatentFactor,
      itemFactors: LatentFactor,
      bias: Float,
      minRating: Float,
      maxRating: Float): Float = {
    math.min(maxRating, math.max(minRating, getRating(userFactors, itemFactors, bias)))
  }

  def getRating(
      userFactors: LatentFactor,
      itemFactors: LatentFactor,
      bias: Float): Float = {
    val dot = VectorUtils.dot(userFactors.vector, itemFactors.vector)
    dot + userFactors.bias + itemFactors.bias + bias
  }
}
