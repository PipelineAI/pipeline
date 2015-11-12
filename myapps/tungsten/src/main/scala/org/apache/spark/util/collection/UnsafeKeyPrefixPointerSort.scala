
package org.apache.spark.util.collection

import scala.io.Source
import java.io._

object UnsafeKeyPrefixPointerSort {
  def main(args : Array[String]) {
    // Create array of 100-byte records
    // key = 10 bytes
    // padding = 2 bytes
    // pointer to value = 4 bytes
    //
    val filename = args(0)

    // Extract the (key,value) tuples
    val keyValueArray: Array[(String, String)] = Source.fromFile(filename).getLines().toArray.map(line => {
      (line.substring(0,10), line.substring(10,98))
    })
    //println("before first: [" + keyValueArray(0)._1 + "]")
    //println("before last: [" + keyValueArray(keyValueArray.length-1)._1 + "]")

    // Extract the 4-byte key prefixes
    val keys = keyValueArray.map(keyValue => keyValue._1.substring(0,4))

    println("before first: [" + keys(0) + "]")
    println("before last: [" + keys(keys.length-1) + "]")

    new Sorter(new StringArraySortDataFormat)
      .sort(keys, 0, 999999, Ordering.String)

    println("after first: [" + keys(0) + "]")
    println("after last: [" + keys(keys.length-1) + "]")

    val file = new File("/tmp/tungsten-unsafekeyprefix.out")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(keys.mkString("\n"))
    bw.close()
  }
}
