package com.advancedspark.tungsten.tuple

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

case class MyTuple(left: Int, right: Int)

object CacheNaiveCaseClassCounters {
  var tuple = new MyTuple(0,0)
	
  val startLatch = new CountDownLatch(1)
  var finishLatch = new CountDownLatch(0) 

  def getValue() : MyTuple = {
    tuple
  }

  def increment(leftIncrement: Int, rightIncrement: Int) : MyTuple = {
    tuple.synchronized {
      tuple = new MyTuple(tuple.left + leftIncrement, tuple.right + rightIncrement)
      tuple
    }
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

    System.out.println("leftInt OK? " + (value.left == leftIncrement * numThreads))
    System.out.println("rightInt OK? " + (value.right == rightIncrement * numThreads))
    System.out.println("runtime? " + (endTime - startTime))
  }
}
