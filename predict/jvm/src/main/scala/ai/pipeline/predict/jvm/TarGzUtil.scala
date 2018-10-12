package ai.pipeline.predict.jvm

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

object TarGzUtil {
  val BUFFER = 2048
  
  def extract(filename: String, directory: String): Unit = {
    val fin = new FileInputStream(filename)
    val in = new BufferedInputStream(fin)
    val gzIn = new GzipCompressorInputStream(in)
    val tarIn = new TarArchiveInputStream(gzIn)
     
    var entry: TarArchiveEntry = tarIn.getNextEntry().asInstanceOf[TarArchiveEntry]
    
    /** Read the tar entries using the getNextEntry method **/    
    while (entry != null) {    
      /** If the entry is a directory, create the directory. **/  
      if (entry.isDirectory()) {   
        val f = new File(directory + entry.getName())
        f.mkdirs()
      }

      /**
      * If the entry is a file,write the decompressed file to the disk
      * and close destination stream.
      **/
      else {
        var count = 0;
        val data: Array[Byte] = new Array[Byte](BUFFER)
    
        val fos = new FileOutputStream(directory + entry.getName())
        val dest = new BufferedOutputStream(fos, BUFFER)
        count = tarIn.read(data, 0, BUFFER)
        while (count >= 0) {
          dest.write(data, 0, count)
          count = tarIn.read(data, 0, BUFFER)
        }
        dest.close()
      }

      entry = tarIn.getNextEntry().asInstanceOf[TarArchiveEntry]  
    }
    
    /** Close the input stream **/    
    tarIn.close()   
  }  
}