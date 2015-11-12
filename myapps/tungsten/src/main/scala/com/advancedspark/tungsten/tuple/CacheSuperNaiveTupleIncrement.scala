package com.advancedspark.tungsten.tuple

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

case class MyTuple(left: Int, right: Int)

object CacheSuperNaiveTupleIncrement {
  var tuple = new MyTuple(0,0)
	
  // a count down latch of 1 is a way to synchronize the start of multiple threads
  // the controller (a test) will call startLatch.countDown() to start the threads
  // assuming all threads call await() at the start of their run()/call() method
  val startLatch = new CountDownLatch(1)
  var finishLatch = new CountDownLatch(0) 

  def getValue() : MyTuple = {
    tuple
  }

  def increment(leftIncrement: Int, rightIncrement: Int) : MyTuple = {
    this.synchronized {
      tuple = new MyTuple(tuple.left + leftIncrement, tuple.right + rightIncrement)
      tuple
    }
  }

  class IncrementTask(leftIncrement: Int, rightIncrement: Int) extends Runnable {
    @Override
    def run() : Unit = {
      //try {
        startLatch.await()
        
        val value = increment(leftIncrement, rightIncrement)

        //System.out.println("success [left:" + (value >>> 32).toInt + ", right:" + value.toInt + "]")
				
        finishLatch.countDown()
      //} catch (InterruptedException e) {
      //  e.printStackTrace()
      //}
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
