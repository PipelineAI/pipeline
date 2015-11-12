package com.advancedspark.tungsten.tuple

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

object CacheFriendlyTupleIncrement {
  // a single, master Long (8-bytes) will maintain 2 separate Ints (4-bytes each)
  val tuple = new AtomicLong()
	
  // a count down latch of 1 is a way to synchronize the start of multiple threads
  // the controller (a test) will call startLatch.countDown() to start the threads
  // assuming all threads call await() at the start of their run()/call() method
  val startLatch = new CountDownLatch(1)
  var finishLatch = new CountDownLatch(5) 

  def getLong() : Long = {
    tuple.get()
  }

  def increment(leftIncrement: Int, rightIncrement: Int) : Unit = {
    var originalLong = 0L
    var updatedLong = 0L
		
    do {
      originalLong = tuple.get()

      // get the right int
      val originalRightInt = originalLong.toInt
			
      // shift right and get the left int
      val originalLeftInt = (originalLong >>> 32).toInt

      // increment the right int by right 
      val updatedRightInt = originalRightInt + rightIncrement

      // increment the left int by left
      val updatedLeftInt = originalLeftInt + leftIncrement

      // TODO:  make this 1 operation
      // update the new single, master long with the new left int 
      updatedLong = updatedLeftInt

      // shift left to setup the right int
      updatedLong = updatedLong << 32

      // set the new right int 
      updatedLong += updatedRightInt
    }	
    while (tuple.compareAndSet(originalLong, updatedLong) == false)
  }

  class IncrementTask(leftIncrement: Int, rightIncrement: Int) extends Runnable {
    @Override
    def run() : Unit = {
      //try {
        startLatch.await()
        System.out.println("success: " + increment(leftIncrement, rightIncrement))

        val masterLong = getLong()
        System.out.println("left:" + (masterLong >>> 32).toInt + ", right:" + masterLong.toInt)
				
        finishLatch.countDown()
      //} catch (InterruptedException e) {
      //  e.printStackTrace()
      //}
    }
  }
	
  def main(args: Array[String]) {
    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
		
    // number of threads = 5
    // left increment = 3
    // right incrmenet = 2

    // schedule all threads in the threadpool
    for (i <- 1 to 5) {
      executor.execute(new IncrementTask(3, 2))
    } 

    // start all threads
    startLatch.countDown()

    // wait for all threads to finish
    finishLatch.await()
		
    val masterLong = getLong()
  
    System.out.println("leftInt OK? " + ((masterLong >>> 32).toInt == 3 * 5)); 
    System.out.println("rightInt OK? " + (masterLong.toInt == 2 * 5)); 
  }
}
