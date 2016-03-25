package com.advancedspark.streaming.rating.core

case class Rating(userId: Int, itemId: Int, rating: Int, geo: String, timestamp: Long)
