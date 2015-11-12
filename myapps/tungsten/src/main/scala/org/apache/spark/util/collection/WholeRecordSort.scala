
package org.apache.spark.util.collection

import scala.io.Source
import java.io._

object WholeRecordSort {
  def main(args : Array[String]) {
    // Create array of 100-byte records
    // key = 10 bytes
    // padding = 2 bytes
    // pointer to value = 4 bytes
    //
    val filename = args(0)

    // Extract the (key,value) tuples
    val keyValues: Array[String] = Source.fromFile(filename).getLines().toArray
//.map(line => {
//      (line.substring(0,10), line.substring(10,98))
//    })

    // Extract the keys
    //val keys = keyValueArray.map(keyValue => keyValue._1)

    //val keyValues = keyValueArray.map(keyValue => keyValue._1 + keyValue._2)

    println("before first: [" + keyValues(0) + "]")
    println("before last: [" + keyValues(keyValues.length-1) + "]")

    new Sorter(new StringArraySortDataFormat)
      .sort(keyValues, 0, 999999, Ordering.String)

    println("after first: [" + keyValues(0) + "]")
    println("after last: [" + keyValues(keyValues.length-1) + "]")

    val file = new File("/tmp/tungsten-wholerecord.out")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(keyValues.mkString("\n"))
    bw.close()
  }
}

class StringArraySortDataFormat extends SortDataFormat[String, Array[String]] {

  override protected def getKey(data: Array[String], pos: Int): String = {
    data(pos)
  }

  override def swap(data: Array[String], pos0: Int, pos1: Int): Unit = {
    val tmp = data(pos0)
    data(pos0) = data(pos1)
    data(pos1) = tmp
  }

  override def copyElement(src: Array[String], srcPos: Int, dst: Array[String], dstPos: Int) {
    dst(dstPos) = src(srcPos)
  }

  override def copyRange(src: Array[String], srcPos: Int, dst: Array[String], dstPos: Int, length: Int) {
    System.arraycopy(src, srcPos, dst, dstPos, length)
  }

  override def allocate(length: Int): Array[String] = {
    new Array[String](length)
  }
}
