package com.advancedspark.ml

import com.twitter.algebird.MinHasher
import com.twitter.algebird.MinHasher32
import com.twitter.algebird.MinHashSignature

object Similarity {
  def getJaccardSimilarity(item1: TaggedItem, item2: TaggedItem): Double = {
    val intersectTags = item1.tags intersect item2.tags
    val unionTags = item1.tags union item2.tags

    val numIntersectTags = intersectTags.size
    val numUnionTags = unionTags.size
    val jaccardSimilarity =
      if (numUnionTags > 0) numIntersectTags.toDouble / numUnionTags.toDouble
      else 0.0

    jaccardSimilarity
  }

  def getApproxLSHSimilarity(minHasher: MinHasher32, item1: TaggedItem, item2: TaggedItem): Double = {
    val minHashSignatureItem1 = item1.tags.map(tag => minHasher.init(tag))
      .reduce((leftMinHashSignature, rightMinHashSignature) => minHasher.plus(leftMinHashSignature, rightMinHashSignature))

    val minHashSignatureItem2 = item2.tags.map(tag => minHasher.init(tag))
      .reduce((leftMinHashSignature, rightMinHashSignature) => minHasher.plus(leftMinHashSignature, rightMinHashSignature))

    minHasher.similarity(minHashSignatureItem1, minHashSignatureItem2)
  }
}
