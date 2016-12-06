package com.advancedspark.streaming.rating.ml.incremental.model

//////////////////////////////////////////////////////////////////////
// This code has been adapted from the following source:
//   https://github.com/brkyvz/streaming-matrix-factorization
// Thanks, Burak!
//////////////////////////////////////////////////////////////////////

import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.ml.Model
import org.apache.spark.ml.util.MLWritable
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD

class LatentMatrixFactorizationModel(
    val rank: Int,
    val userFactors: IndexedRDD[Long, LatentFactor], // bias and the user row
    val itemFactors: IndexedRDD[Long, LatentFactor], // bias and the item row
    val globalBias: Float,
    val minRating: Float,
    val maxRating: Float) 
    //extends Model with MLWritable 
{

  /** Predict the rating of one user for one item. */
  def predict(userId: Long, itemId: Long): Float = {
    LatentMatrixFactorizationModelOps.predict(userId, itemId, userFactors.get(userId), itemFactors.get(itemId), globalBias,
      minRating, maxRating).rating
  }

  /**
   * Predict the rating of many users for many items.
   * The output RDD will return a prediction for all user - item pairs. For users or
   * items that were missing from the training data, the prediction will be made with the global
   * bias (global average) +- the user or item bias, if they exist.
   *
   * @param usersItems  RDD of (user, item) pairs.
   * @return RDD of Ratings.
   */
  def predict(usersItems: RDD[(Long, Long)]): RDD[Rating[Long]] = {
    val users = usersItems.leftOuterJoin(userFactors).map { case (userId, (itemId, userFactorsForSingleUser)) =>
      (itemId, (userId, userFactorsForSingleUser))
    }
    val sc = usersItems.sparkContext
    val globalAvg = sc.broadcast(globalBias)
    val min = sc.broadcast(minRating)
    val max = sc.broadcast(maxRating)
    users.leftOuterJoin(itemFactors).map { case (itemId, ((userId, userFactorsForSingleUser), itemFactorsForSingleUser)) =>
      LatentMatrixFactorizationModelOps.predict(userId, itemId, userFactorsForSingleUser, itemFactorsForSingleUser, globalAvg.value,
        min.value, max.value)
    }
  }

/*
  // TODO:  Members declared in org.apache.spark.ml.util.Identifiable
  val uid: String = ???

  // TODO:  Members declared in org.apache.spark.ml.util.MLWritable
  def write: org.apache.spark.ml.util.MLWriter = ???
   
  // TODO:  Members declared in org.apache.spark.ml.Model
  override def copy(extra: org.apache.spark.ml.param.ParamMap): Nothing = ???

  // TODO:  Members declared in org.apache.spark.ml.PipelineStage
  def transformSchema(schema: org.apache.spark.sql.types.StructType): org.apache.spark.sql.types.StructType = ???

  // TODO:  Members declared in org.apache.spark.ml.Transformer
  def transform(dataset: org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = ???
*/
}
