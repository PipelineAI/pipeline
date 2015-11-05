package com.fluxcapacitor.pipeline.spark.streaming

case class Rating(fromuserid: Int, touserid: Int, rating: Int, batchtime: Long)
