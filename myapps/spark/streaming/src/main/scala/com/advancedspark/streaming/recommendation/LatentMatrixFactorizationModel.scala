package com.advancedspark.streaming.recommendation

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import java.util.Random

import edu.berkeley.cs.amplab.spark.indexedrdd.{KeySerializer, IndexedRDD}
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

import org.apache.spark.Logging
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD

import com.advancedspark.streaming.recommendation.utils.VectorUtils

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
    LatentMatrixFactorizationModel.predict(user, product, uFeatures, pFeatures, globalBias,
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
      LatentMatrixFactorizationModel.predict(user, product, uFeatures, pFeatures, globalAvg.value,
        min.value, max.value)
    }
  }
}

case class StreamingLatentMatrixFactorizationModel(
    override val rank: Int,
    override val userFeatures: IndexedRDD[Long, LatentFactor], // bias and the user row
    override val productFeatures: IndexedRDD[Long, LatentFactor], // bias and the product row
    override val globalBias: Float,
    observedExamples: Long,
    override val minRating: Float,
    override val maxRating: Float)
  extends LatentMatrixFactorizationModel(rank, userFeatures, productFeatures,
    globalBias, minRating, maxRating)

object LatentMatrixFactorizationModel extends Serializable with Logging {

  /**
   * Adds random factors for missing user - product entries and updates the global bias and
   * number of observed examples. Returns the initialized model, and number of examples in this rdd.
   */
  def initialize(
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
     
    // Generate random entries for missing user-product factors
    val usersAndRatings = ratings.map(r => (r.user, r))
    val productsAndRatings = ratings.map(r => (r.item, r))
    val sc = ratings.sparkContext
    var userFeatures = initialModel match {
      case Some(model) => model.userFeatures
      case None =>
        IndexedRDD(sc.parallelize(Seq.empty[(Long, LatentFactor)], ratings.partitions.length))
    }

    var prodFeatures = initialModel match {
      case Some(model) => model.productFeatures
      case None =>
        IndexedRDD(sc.parallelize(Seq.empty[(Long, LatentFactor)], ratings.partitions.length))
    }

    userFeatures = IndexedRDD(usersAndRatings.fullOuterJoin(userFeatures)
      .mapPartitionsWithIndex { case (partitionId, iterator) =>
        randGenerator.setSeed(seed + 2 << 16 + partitionId)
        iterator.map { case (user, (rating, uFeatures)) =>
          (user, uFeatures.getOrElse(randGenerator.nextValue()))
        }
    })

    prodFeatures = IndexedRDD(productsAndRatings.fullOuterJoin(prodFeatures)
      .mapPartitionsWithIndex { case (partitionId, iterator) =>
        randGenerator.setSeed(seed + 2 << 32 + partitionId)
        iterator.map { case (user, (rating, pFeatures)) =>
          (user, pFeatures.getOrElse(randGenerator.nextValue()))
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
        new StreamingLatentMatrixFactorizationModel(rank, userFeatures, prodFeatures,
          streaming.globalBias, streaming.observedExamples, minRating, maxRating)
      case _ =>
        if (isStreaming) {
          new StreamingLatentMatrixFactorizationModel(rank, userFeatures, prodFeatures,
            globalBias, numExamples, minRating, maxRating)
        } else {
          new LatentMatrixFactorizationModel(rank, userFeatures, prodFeatures, globalBias,
            minRating, maxRating)
        }
    }
    (initializedModel, numRatings)
  }

  def predict(
      user: Long,
      product: Long,
      uFeatures: Option[LatentFactor],
      pFeatures: Option[LatentFactor],
      globalBias: Float,
      minRating: Float,
      maxRating: Float): Rating[Long] = {
    val finalRating =
      if (uFeatures.isDefined && pFeatures.isDefined) {
        Rating(user, product, LatentMatrixFactorizationModel.getRating(uFeatures.get, pFeatures.get,
          globalBias, minRating, maxRating))
      } else if (uFeatures.isDefined) {
        logWarning(s"Product data missing for product id $product. Will use user factors.")
        val rating = globalBias + uFeatures.get.bias
        Rating(user, product, math.min(maxRating, math.max(minRating, rating)))
      } else if (pFeatures.isDefined) {
        logWarning(s"User data missing for user id $user. Will use product factors.")
        val rating = globalBias + pFeatures.get.bias
        Rating(user, product, math.min(maxRating, math.max(minRating, rating)))
      } else {
        logWarning(s"Both user and product factors missing for ($user, $product). " +
          "Returning global average.")
        val rating = globalBias
        Rating(user, product, math.min(maxRating, math.max(minRating, rating)))
      }
    finalRating
  }

  def getRating(
      userFeatures: LatentFactor,
      prodFeatures: LatentFactor,
      bias: Float,
      minRating: Float,
      maxRating: Float): Float = {
    math.min(maxRating, math.max(minRating, getRating(userFeatures, prodFeatures, bias)))
  }

  def getRating(
      userFeatures: LatentFactor,
      prodFeatures: LatentFactor,
      bias: Float): Float = {
    val dot = VectorUtils.dot(userFeatures.vector, prodFeatures.vector)
    dot + userFeatures.bias + prodFeatures.bias + bias
  }
}

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
