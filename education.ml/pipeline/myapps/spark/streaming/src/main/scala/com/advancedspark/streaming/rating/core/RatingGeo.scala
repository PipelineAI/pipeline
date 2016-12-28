package com.advancedspark.streaming.rating.core

case class RatingGeo(userId: Int, itemId: Int, rating: Float, timestamp: Long, geoCity: String)
