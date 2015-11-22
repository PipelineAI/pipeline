package com.advancedspark.pipeline.spark.streaming

case class Rating(fromuserid: Int, touserid: Int, rating: Int, batchtime: Long)
