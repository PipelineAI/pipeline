package com.advancedspark.core.sort 

import java.util.Comparator
import scala.io.Source
import java.io._
import java.util.Arrays
import scala.collection.mutable.ListBuffer
//import com.google.common.primitives.UnsignedBytes

object CacheNaiveSort {
  def main(args : Array[String]) = {
    val byteArrayLength = args(0).toInt
    val numIters = args(1).toInt

    // Set up the timings collection used to avg later across all the runs
    var timings = new ListBuffer[Double]()

    // Read the data set and retrieve only the desired number of bytes to sort
    var byteArrays = Source.fromFile("../../datasets/sort/sort.txt").getLines
      .flatMap(record => Array(record.substring(0, byteArrayLength).getBytes)).toArray

    // Do 5 runs and avg the timings
    (1 to numIters).foreach(run => {
      val startTime = System.currentTimeMillis    
        
      // Do the Sort!
      Arrays.sort(byteArrays, 
        //UnsignedBytes.lexicographicalComparator()
        new ByteArrayComparator(byteArrayLength)
      )
        
      val endTime = System.currentTimeMillis
      val timing = endTime - startTime
      timings += timing 
    })

    val avgTiming = timings.sum / timings.size
    System.out.println(s"""Elapsed avg time for numIters ${numIters} and byteArrayLength ${byteArrayLength}:  ${(avgTiming)}""")

    val file = new File(s"""out-${byteArrayLength}.txt""")
    val bw = new BufferedWriter(new FileWriter(file))
    byteArrays.foreach(record => bw.write(record.toString + "\n"))
    bw.close()
  }

  class ByteArrayComparator(val byteArrayLength: Int) extends Comparator[Array[Byte]] {
    def compare(left: Array[Byte], right: Array[Byte]): Int = {
      for (i <- 0 until byteArrayLength) {
        val a = (left(i) & 0xff);
        val b = (right(i) & 0xff);
        if (a != b) {
          a - b;
        }
      }      
      0
    }
  }
}
