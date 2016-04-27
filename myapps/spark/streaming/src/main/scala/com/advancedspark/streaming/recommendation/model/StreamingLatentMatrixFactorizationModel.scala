package com.advancedspark.streaming.recommendation.model

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

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
