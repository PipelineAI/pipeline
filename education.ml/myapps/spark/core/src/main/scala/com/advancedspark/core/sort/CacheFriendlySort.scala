package com.advancedspark.core.sort 

import java.util.Comparator
import scala.io.Source
import java.io._
import java.util.Arrays
import scala.collection.mutable.ListBuffer
import com.google.common.primitives.UnsignedBytes
import sun.misc.Unsafe
import java.lang.reflect.Field
import org.apache.spark.unsafe.Platform
import org.apache.spark.unsafe.types.ByteArray
import org.apache.spark.unsafe.memory.UnsafeMemoryAllocator

case class Address(value: Long)

object CacheFriendlySort {
  def main(args : Array[String]) = {
    val byteArrayLength = args(0).toInt
    val numIters = args(1).toInt

    // Set up the timings collection used to avg later across all the runs
    var timings = new ListBuffer[Double]()

    val dataWorkHome = sys.env("DATA_WORK_HOME")
    val datasetsHome = sys.env("DATASETS_HOME")

    // Read the data set and retrieve only the desired number of bytes to sort
    var byteArrays = Source.fromFile(s"""${datasetsHome}/sort/sort.txt""").getLines
      .flatMap(record => Array(record.substring(0, byteArrayLength).getBytes)).toArray

    val totalNumRecords = byteArrays.size
    val totalNumBytes = totalNumRecords * byteArrayLength

    val unsafeMemoryAllocator = new UnsafeMemoryAllocator()
    val memoryBlock = unsafeMemoryAllocator.allocate(totalNumBytes)

    val absoluteBaseAddress = memoryBlock.getBaseOffset
    val recordBaseAddresses = new Array[Address](totalNumRecords)
 
    for (i <- 0 until totalNumRecords) {
      // Calculate and record the record base address
      val recordBaseAddress = absoluteBaseAddress + (i * byteArrayLength)
      recordBaseAddresses(i) = Address(recordBaseAddress)
     
      // Add each byte to the unsafe, pre-allocated byte array 
      val byteArray = byteArrays(i)
      ByteArray.writeToMemory(byteArray, null, recordBaseAddress)
    }

    // Do 5 runs and avg the timings
    (1 to numIters).foreach(run => {
      val startTime = System.currentTimeMillis    
        
      // Do the Sort!
      Arrays.sort(recordBaseAddresses, 
        //UnsignedBytes.lexicographicalComparator()
        new ByteArrayAddressComparator(byteArrayLength)
      )
        
      val endTime = System.currentTimeMillis
      val timing = endTime - startTime
      timings += timing 
    })

    val avgTiming = timings.sum / timings.size
    System.out.println(s"""Elapsed avg time for numIters ${numIters} and byteArray length ${byteArrayLength}:  ${(avgTiming)}""")
    
    val file = new File(s"""${dataWorkHome}/core/sorted-friendly-${byteArrayLength}.out""")
    val bw = new BufferedWriter(new FileWriter(file))
    recordBaseAddresses.foreach(recordBaseAddress => {
      val byteArray = new Array[Byte](byteArrayLength)
      for (i <- 0 until byteArrayLength) {
        byteArray(i) = Platform.getByte(null, recordBaseAddress.value + i)
      }
      bw.write(byteArray.toString + "\n")
    })
    bw.close()

    unsafeMemoryAllocator.free(memoryBlock)
  }

  class ByteArrayAddressComparator(byteArrayLength: Int) extends Comparator[Address] {
    def compare(leftRecordBaseAddress: Address, rightRecordBaseAddress: Address): Int = {
      for (i <- 0 until byteArrayLength) {
        val a = Platform.getByte(null, leftRecordBaseAddress.value + i) & 0xff
        val b = Platform.getByte(null, rightRecordBaseAddress.value + i) & 0xff
        if (a != b) {
          a - b
        }
      }      
      0
    }
  }
}
