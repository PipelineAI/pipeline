package com.advancedspark.streaming.ml.incremental

import java.io.PrintWriter
import org.apache.spark.{SparkContext, Logging}
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.ml.recommendation.ALS.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.api.java.JavaDStream
import org.apache.spark.streaming.dstream.DStream

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

import com.advancedspark.streaming.ml.incremental.optimization.MFGradientDescent
import com.advancedspark.streaming.ml.incremental.model.StreamingLatentMatrixFactorizationModel
import com.advancedspark.streaming.ml.incremental.model.LatentMatrixFactorizationModelOps
import com.advancedspark.streaming.ml.incremental.model.LatentFactorGenerator
import com.advancedspark.streaming.ml.incremental.model.LatentFactor

/**
 * Trains a Matrix Factorization Model for Recommendation Systems. The model consists of
 * user factors (User Matrix, `U`), product factors (Product Matrix, `P^T^`),
 * user biases (user bias vector, `bu`), product biases (product bias vector, `bp`) and
 * the global bias (global average, `mu`).
 *
 * Trained on a DStream, but can make predictions on a DStream or RDD.
 *
 * @param params Parameters for training
 */
class StreamingLatentMatrixFactorization(params: LatentMatrixFactorizationParams)
  extends LatentMatrixFactorization(params) {

  def this() = this(new LatentMatrixFactorizationParams)

  /** Return the latest model. */
  def latestModel() = {
    model.get.asInstanceOf[StreamingLatentMatrixFactorizationModel]
  }

  /**
   * Update the model by training on batches of data from a DStream.
   * This operation registers a DStream for training the model,
   * and updates the model based on every subsequent
   * batch of data from the stream.
   *
   * @param data DStream containing Ratings
   */
  def trainOn(data: DStream[Rating[Long]]): Unit = {
    data.foreachRDD { (rdd, time) =>
      if (!model.isEmpty) {
        val (initialModel, numExamples) =
          LatentMatrixFactorizationModelOps.initialize(rdd, params, model, isStreaming = true)
        model = Some(optimizer.train(rdd, initialModel, numExamples).
          asInstanceOf[StreamingLatentMatrixFactorizationModel])
        logInfo(s"Model updated - time $time")
      } else {
        logInfo(s"Model not updated since it's empty - time $time")
      }
    }
  }
}
