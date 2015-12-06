package com.advancedspark.core.thread

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

object LockFreeAtomicLongCounters {
  // a single Long (8-bytes) will maintain 2 separate Ints (4-bytes each)
  val counters = new AtomicLong()
	
  val startLatch = new CountDownLatch(1)
  var finishLatch: CountDownLatch = null // this will be set to the # of threads

  def getCounters(): Long = {
    counters.get()
  }

  def increment(leftIncrement: Int, rightIncrement: Int): Unit = {
    var originalCounters = 0L
    var updatedCounters = 0L

    do {
      originalCounters = getCounters()

      // get the right int
      val originalRightInt = originalCounters.toInt

      // shift right and get the left int
      val originalLeftInt = (originalCounters >>> 32).toInt

      // increment the right int by rightIncrement
      val updatedRightInt = originalRightInt + rightIncrement

      // increment the left int by leftIncrement
      val updatedLeftInt = originalLeftInt + leftIncrement

      // set the new left int
      updatedCounters = updatedLeftInt

      // shift left to setup the right int
      updatedCounters = updatedCounters << 32

      // set the new right int
      updatedCounters += updatedRightInt
    }
    while (counters.compareAndSet(originalCounters, updatedCounters) == false)
  }

  class IncrementTask(leftIncrement: Int, rightIncrement: Int) extends Runnable {
    @Override
    def run() : Unit = {
      startLatch.await()
        
      increment(leftIncrement, rightIncrement)

      finishLatch.countDown()
    }
  }
	
  def main(args: Array[String]) {
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val numThreads = args(0).toInt
    val leftIncrement = args(1).toInt
    val rightIncrement = args(2).toInt

    finishLatch = new CountDownLatch(numThreads)

    // schedule all threads in the threadpool
    for (i <- 1 to numThreads) {
      executor.execute(new IncrementTask(leftIncrement, rightIncrement))
    }

    val startTime = System.currentTimeMillis

    // start all threads
    startLatch.countDown()

    // wait for all threads to finish
    finishLatch.await()

    val endTime = System.currentTimeMillis

    val counters = getCounters()

    System.out.println("leftInt OK? " + ((counters >>> 32).toInt == leftIncrement * numThreads))
    System.out.println("rightInt OK? " + (counters.toInt == rightIncrement * numThreads))
    System.out.println("runtime? " + (endTime - startTime))
  }
}
