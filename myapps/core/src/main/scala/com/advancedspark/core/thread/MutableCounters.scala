package com.advancedspark.core.thread

class MutableCounters(var left: Int, var right: Int) {
  def increment(leftIncrement: Int, rightIncrement: Int) = {
    this.synchronized {
      left += leftIncrement
      right += rightIncrement
    }
  }

  def getCountersTuple(): (Int, Int) = {
    this.synchronized {
      (left, right)
    }
  }
}
