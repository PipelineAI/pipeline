
package org.apache.spark.util.collection

import scala.io.Source
import java.io._

object Sort {
  def main(args : Array[String]) {
    // Create array of 100-byte records
    // key = 10 bytes
    // padding = 2 bytes
    // pointer to value = 4 bytes
    //
    val filename = args(0)

    // Extract the (key,value) tuples
    val keyValueArray: Array[(String, String)] = Source.fromFile(filename).getLines().toArray.map(line => {
      println(line)
      (line.substring(0,9), line.substring(10,98))
    })
    println(keyValueArray(0)._1)
    println(keyValueArray(keyValueArray.length-1)._1)

    // Extract the keys
    val keys = keyValueArray.map(keyValue => keyValue._1)

    new Sorter(new StringArraySortDataFormat)
      .sort(keys, 0, 999999, Ordering.String)

    println(keys(0))
    println(keys(keys.length-2))

    val file = new File("/tmp/tungsten.out")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(keys.mkString("\n"))
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
