package com.advancedspark.streaming.rating.core

case class RatingGeo(userId: Int, itemId: Int, rating: Int, timestamp: Long, geoCity: String)
