package com.advancedspark.ml

case class TaggedItem(id: Long, title: String, tags: Seq[String]) {
  override def toString: String = id + ", " + title + ", " + tags
}
