package com.advancedspark.codegen.example

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes.JAVA_STRING
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.spark.ml.classification.DecisionTreeClassificationModelCodeGenerator

object TreeClassifierMain {
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
    lookupMap.put("c", (10005, 10006))
    
    ctx.addReferenceObj("lookupMap", lookupMap, lookupMap.getClass.getName)

    ctx.addNewFunction("lookup", "public Object lookup(Object key) { return lookupMap.get(key); }")
    
    //val rootNode = new org.apache.spark.ml.tree.InternalNode()
    //val depth = 
    //val (initialFunc, remainingFuncs) = DecisionTreeClassificationModelCodeGenerator
    //  .generateSourceCode(rootNode, depth)
    
    // TODO:  Add the remaining functions to the ctx with addNewFunction()
    //ctx.addNewFunction("treeFunc1", initialFunc.toString())  
    
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
      new CodeGenBundle("com.advancedspark.codegen.example.generated", "TreeClassifier", 
          null, 
          Array(
              classOf[Initializable], 
              classOf[Lookupable], 
              classOf[Serializable], 
              classOf[org.apache.spark.ml.linalg.Vector],
              classOf[java.util.Arrays]
              //classOf[java.util.HashSet]
          ),
          Array(classOf[java.util.HashMap[Any, Any]]), 
          CodeFormatter.stripExtraNewLines(source), 
          ctx.getPlaceHolderToComments())
    )

    try {
      // TODO:  Make this more like com.advancedspark.codegen.CodeGenerator using CodeGenBundle

      val clazz = DecisionTreeClassificationModelCodeGenerator.compile(cleanedSource.body, cleanedSource.interfaces)
      System.out.println(s"\n${CodeFormatter.format(cleanedSource)}")

      val references = ctx.references.toArray
      
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references)

      System.out.println(s"Lookup 'a' -> '${bar.asInstanceOf[Lookupable].lookup("a")}'")

      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.TreeClassifier")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(references)
      System.out.println(s"Lookup 'b' -> '${bar2.asInstanceOf[Lookupable].lookup("b")}'")
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${cleanedSource}", e)
    }
  }
}
