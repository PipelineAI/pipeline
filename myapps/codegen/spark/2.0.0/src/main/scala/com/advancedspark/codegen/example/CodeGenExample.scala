package com.advancedspark.codegen.example

import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.CodeGenUtils._
import com.advancedspark.codegen.CodeAndComment
import com.advancedspark.codegen.CodeFormatter

trait Foo {
  def init(): Unit
}

object CodeGenExample {
  import CodeGenTypes._

  def main(args: Array[String]) {   
    val ctx = new CodeGenContext()
    
    ctx.addMutableState(JAVA_STRING, "value", "value = \"\";")
    
    val referenceObj = new Integer(0)
    ctx.addReferenceObj("barReferenceObj", referenceObj, classOf[Integer].getName)

    val source = s"""
      public Object generate(Object[] references) {
        return new Bar(references);
      }

      ${ctx.registerComment("Comment Bar")}
    
      final class Bar implements Foo {
        private Object[] references;
        ${ctx.declareMutableStates()}

        public Bar(Object[] references) {
          this.references = references;
        }

        public void init() {
          ${ctx.initMutableStates()}
        }

        ${ctx.declareAddedFunctions()}
      }
      """.trim

    // format and compile source      
    val cleanedSource = CodeFormatter.stripOverlappingComments(
      new CodeAndComment("com.advancedspark.codegen.example", CodeFormatter.stripExtraNewLines(source), ctx.getPlaceHolderToComments()))

    try {
      val clazz = CodeGenerator.compile(cleanedSource)
      System.out.println(s"\n${CodeFormatter.format(cleanedSource)}")

      val references = ctx.references.toArray
      
      val bar = clazz.generate(references).asInstanceOf[Foo]

      System.out.println(bar)
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${cleanedSource}", e)
    }
    

      

  }
}