package com.advancedspark.codegen.example

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes._
import com.advancedspark.codegen.CodeGenerator

trait Initializable {
  def initialize(references: Array[Any]): Unit
}

trait Lookupable {
  def lookup(key: Any): Any
}

object LookupMapMain {
  def main(args: Array[String]) {   
    val ctx = new CodeGenContext()
    
    ctx.addMutableState(JAVA_STRING, "str", "str = \"blahblah\";")

    val lookupMap = new java.util.HashMap[Any, Any]()

    // TODO:  To lower the memory footprint, and improve cache locality, 
    //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
    //        
    // String :: primitive int array
    lookupMap.put("a", (10001, 10002))
    lookupMap.put("b", (10003, 10004))
    
    ctx.addReferenceObj("lookupMap", lookupMap, lookupMap.getClass.getName)

    ctx.addNewFunction("lookup", "public Object lookup(Object key) { return lookupMap.get(key); }")
       
    // TODO:  Disable comments and line numbers as they're expensive
    val source = s"""
      ${ctx.registerComment("LookupMap Comment...")}
    
      ${ctx.declareMutableStates()}

      public void initialize(Object[] references) {
        ${ctx.initMutableStates()}
      }

      ${ctx.declareAddedFunctions()}
      """.trim

    // Format and compile source
    // Note:  If you see "InstantiationException", you might be trying to create a package+classname that already exists.
    //        This is why we're namespacing this package to include ".generated", but we also changed the name of this
      //        outer class to LookupMapMain to make this more explicit.
    val cleanedSource = CodeFormatter.stripOverlappingComments(        
      new CodeGenBundle("com.advancedspark.codegen.example.generated", "LookupMap", 
          null, 
          Array(classOf[Initializable], classOf[Lookupable]), 
          Array(classOf[java.util.HashMap[Any, Any]]), 
          CodeFormatter.stripExtraNewLines(source), 
          ctx.getPlaceHolderToComments())
    )

    try {
      val clazz = CodeGenerator.compile(cleanedSource)
      System.out.println(s"\n${CodeFormatter.format(cleanedSource)}")

      val references = ctx.references.toArray
      
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references)

      System.out.println(s"Lookup 'a' -> '${bar.asInstanceOf[Lookupable].lookup("a")}'")
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${cleanedSource}", e)
    }
  }
}