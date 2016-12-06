package com.advancedspark.codegen.example

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes._
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.DumpByteCode
import com.advancedspark.codegen.Predictable
import com.advancedspark.codegen.Initializable

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.DefaultHttpClient

object PredictorMain {
  def main(args: Array[String]) {   
    val ctx = new CodeGenContext()
    
    ctx.addMutableState(JAVA_STRING, "str", "str = \"blahblah\";")

    val predictMap = new java.util.HashMap[Any, Any]()

    // TODO:  To lower the memory footprint, and improve cache locality, 
    //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
    //        
    // String :: primitive int array
    predictMap.put("a", (10001, 10002))
    predictMap.put("b", (10003, 10004))
    predictMap.put("c", (10005, 10006))
    
    ctx.addReferenceObj("predictMap", predictMap, predictMap.getClass.getName)

    ctx.addNewFunction("predict", "public Object predict(Object key) { return predictMap.get(key); }")
       
    // TODO:  Disable comments and line numbers as they're expensive
    val source = s"""
      ${ctx.declareMutableStates()}

      public void initialize(Object[] references) {
        ${ctx.initMutableStates()}
      }

      ${ctx.declareAddedFunctions()}
      """.trim

    // Format and compile source
    // Note:  If you see "InstantiationException", you might be trying to create a package+classname that already exists.
    //        This is why we're namespacing this package to include ".generated", but we also changed the name of this
      //        outer class to PredictableMain to make this more explicit.
    val cleanedSource = 
      new CodeGenBundle("com.advancedspark.codegen.example.generated.Predictor", 
          null, 
          Array(classOf[Initializable], classOf[Predictable], classOf[Serializable]), 
          Array(classOf[java.util.HashMap[Any, Any]]), 
          CodeFormatter.stripExtraNewLines(source) 
      )
    

    try {
      val clazz = CodeGenerator.compile(cleanedSource)
      System.out.println(s"\n${CodeFormatter.format(cleanedSource)}")

      val references = ctx.references.toArray
      
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references)

      System.out.println(s"Predict 'a' -> '${bar.asInstanceOf[Predictable].predict("a")}'")

      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.Predictor")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(references)
      System.out.println(s"Predict 'b' -> '${bar2.asInstanceOf[Predictable].predict("b")}'")

      // create an HttpPost object
      println("--- HTTP POST UPDATE JAVA CLASS ---")
      val post = new HttpPost(s"http://prediction.demo.datasticks.com/update-source/com.advancedspark.codegen.example.generated.Predictor")

      // set the Content-type
      post.setHeader("Content-type", "text/plain")

      // add the byte[] as a ByteArrayEntity
      post.setEntity(new StringEntity(cleanedSource.body))

      // send the post request
      val response = (new DefaultHttpClient).execute(post)

      // print the response status and headers
      println("--- HTTP RESPONSE STATUS ---")
      println(response.getStatusLine)

      println("--- HTTP RESPONSE HEADERS ---")
      response.getAllHeaders.foreach(arg => println(arg))
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${cleanedSource}", e)
    }
  }
}
