package com.advancedspark.tungsten.tuple

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

object CacheFriendlyLockFreeCounters {
  // a single Long (8-bytes) will maintain 2 separate Ints (4-bytes each)
  val tuple = new AtomicLong()
	
  val startLatch = new CountDownLatch(1)
  var finishLatch = new CountDownLatch(0) 

  def getValue() : Long = {
    tuple.get()
  }

  def increment(leftIncrement: Int, rightIncrement: Int) : Long = {
    var originalLong = 0L
    var updatedLong = 0L

    do {
      originalLong = getValue()

      // get the right int
      val originalRightInt = originalLong.toInt

      // shift right and get the left int
      val originalLeftInt = (originalLong >>> 32).toInt

      // increment the right int by rightIncrement
      val updatedRightInt = originalRightInt + rightIncrement

      // increment the left int by leftIncrement
      val updatedLeftInt = originalLeftInt + leftIncrement

      // set the new left int
      updatedLong = updatedLeftInt

      // shift left to setup the right int
      updatedLong = updatedLong << 32

      // set the new right int
      updatedLong += updatedRightInt
    }
    while (tuple.compareAndSet(originalLong, updatedLong) == false)

    updatedLong
  }



  class IncrementTask(leftIncrement: Int, rightIncrement: Int) extends Runnable {
    @Override
    def run() : Unit = {
      startLatch.await()
        
      val value = increment(leftIncrement, rightIncrement)

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

    val value = getValue()

    System.out.println("leftInt OK? " + ((value >>> 32).toInt == leftIncrement * numThreads))
    System.out.println("rightInt OK? " + (value.toInt == rightIncrement * numThreads))
    System.out.println("runtime? " + (endTime - startTime))
  }
}
