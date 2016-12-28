package com.advancedspark.ml

import com.twitter.algebird.{ MinHasher, MinHasher32, MinHashSignature }

case class MinHashTaggedItem(id: Long, title: String, tags: Seq[String], combinedTagSignature: MinHashSignature) {
  override def toString: String = id + ", " + title + ", " + tags +  ", " + combinedTagSignature
}
