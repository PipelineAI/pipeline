package com.fluxcapacitor.pipeline.spark.streaming

case class Rating(fromUserId: Int, toUserId: Int, rating: Int, batchtime: Long)
