package com.advancedspark.streaming.rating.ml.incremental.model

import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD
import edu.berkeley.cs.amplab.spark.indexedrdd.IndexedRDD._

case class StreamingLatentMatrixFactorizationModel(
    override val rank: Int,
    override val userFactors: IndexedRDD[Long, LatentFactor], // bias and the user row
    override val itemFactors: IndexedRDD[Long, LatentFactor], // bias and the item row
    override val globalBias: Float,
    observedExamples: Long,
    override val minRating: Float,
    override val maxRating: Float)
  extends LatentMatrixFactorizationModel(rank, userFactors, itemFactors,
    globalBias, minRating, maxRating)
