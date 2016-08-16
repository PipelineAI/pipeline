package com.advancedspark.codegen.example

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes._
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.DumpByteCode

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient

import scala.collection.JavaConverters._

object RecommendationsMain {
  def main(args: Array[String]) {   
    val ctx = new CodeGenContext()
    
    val recommendationMap = new java.util.HashMap[Any, Any]()

    // TODO:  To lower the memory footprint, and improve cache locality, 
    //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
    //        
    // String -> Array[String]
    recommendationMap.put("21619", ("10001", "10002"))
    recommendationMap.put("21620", ("10003", "10004"))
    recommendationMap.put("21621", ("10005", "10006"))
    
    ctx.addReferenceObj("recommendationMap", recommendationMap, recommendationMap.getClass.getName)

    ctx.addNewFunction("lookup", 
        "public Object lookup(Object userId) { return recommendationMap.get(userId); }")
       
    val source = s"""
      ${ctx.declareMutableStates()}

      public void initialize(Object[] references) {
        ${ctx.initMutableStates()}
      }

      ${ctx.declareAddedFunctions()}
      """.trim

    // Format source
    val codeGenBundle = new CodeGenBundle("com.advancedspark.codegen.example.generated.RecommendationMap", 
      null, 
      Array(classOf[Initializable], classOf[Lookupable], classOf[Serializable]), 
      Array(classOf[java.util.HashMap[Any, Any]]), 
      CodeFormatter.stripExtraNewLines(source) 
    )   
      
    // Generate, compile, instantiate, and test source          
    try {
      // Note:  If you see "InstantiationException", you might be trying to create a package+classname that already exists.
      //        This is why we're namespacing this package to include ".generated", but we also changed the name of this
      //        outer class to LookupMapMain to make this more explicit.
      val clazz = CodeGenerator.compile(codeGenBundle)
      System.out.println(s"\n${CodeFormatter.format(codeGenBundle)}")      
            
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(ctx.references.toArray)

      System.out.println(s"Lookup '21619' -> '${bar.asInstanceOf[Lookupable].lookup("21619")}'")

      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.RecommendationMap")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(ctx.references.toArray)
      
      System.out.println(s"Lookup '21620' -> '${bar2.asInstanceOf[Lookupable].lookup("21620")}'")
    
          // create an HttpPost object
      println("--- HTTP POST UPDATE JAVA SOURCE ---")
      val post = new HttpPost(s"http://demo.pipeline.io:9040/update-java/${codeGenBundle.fullyQualifiedClassName}")
  
      // set the Content-type
//      post.setHeader("Content-type", "text/plain")
       
      val baos = new java.io.ByteArrayOutputStream()
      val out = new java.io.ObjectOutputStream(baos)   
      out.writeObject(bar2)
      val bar2Bytes = baos.toByteArray()
      out.close();
      baos.close();
      
      // add the byte[] as a ByteArrayEntity
      post.setEntity(new ByteArrayEntity(bar2Bytes))

      // send the post request
      val response = (new DefaultHttpClient).execute(post)
  
      // print the response status and headers 
      println("--- HTTP RESPONSE STATUS ---")
      println(response.getStatusLine)
      
      println("--- HTTP RESPONSE HEADERS ---")
      response.getAllHeaders.foreach(arg => println(arg))
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${codeGenBundle}", e)
    }
  }
}
