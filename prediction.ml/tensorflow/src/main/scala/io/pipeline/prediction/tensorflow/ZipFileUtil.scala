package io.pipeline.prediction.tensorflow

import java.io._
import java.util._
import java.util.zip._

object ZipFileUtil {
  
  def unzip(filename: String, outputPath: String): Unit = {
    val BUFFER = 2048;
      try {
         var dest: BufferedOutputStream = null
         var is: BufferedInputStream = null
         var entry: ZipEntry = null
         val zipfile: ZipFile = new ZipFile(filename)                 
         val e = zipfile.entries()
         
         while(e.hasMoreElements()) {
            entry = e.nextElement().asInstanceOf[ZipEntry]
            System.out.println("Extracting: " + entry);

            is = new BufferedInputStream(zipfile.getInputStream(entry))

            var count = 0
            val data: Array[Byte] = new Array[Byte](BUFFER)
            val fos = new FileOutputStream(entry.getName())

            dest = new BufferedOutputStream(fos, BUFFER)
            count = is.read(data, 0, BUFFER)
            while (count != -1) {
              dest.write(data, 0, count)           
              count = is.read(data, 0, BUFFER)
            }
            dest.flush()
            dest.close()
            is.close()
         }
      } catch {
        case e: Throwable => {
          e.printStackTrace()
          throw e
        }
      }
   }
}
