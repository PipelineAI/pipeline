/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TODO:  Derived from ... link to spark source code

package com.advancedspark.codegen

import java.io.ByteArrayInputStream
import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.existentials
import org.codehaus.janino.ByteArrayClassLoader
import org.codehaus.janino.ClassBodyEvaluator
import org.codehaus.janino.SimpleCompiler
import org.codehaus.janino.util.ClassFile
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.{Map => JavaMap}
import org.codehaus.janino.SimpleCompiler
import collection.JavaConverters._

object CodeGenTypes {
  final val JAVA_BOOLEAN = "boolean"
  final val JAVA_BYTE = "byte"
  final val JAVA_SHORT = "short"
  final val JAVA_INT = "int"
  final val JAVA_LONG = "long"
  final val JAVA_FLOAT = "float"
  final val JAVA_DOUBLE = "double"
  final val JAVA_STRING = "String"
  final val JAVA_MAP = "Map"
  final val JAVA_ARRAY = "Array"
  final val JAVA_NULL = "null"
}

/**
 * A context for codegen.  
 * Tracks list of objects that could be passed into generated Java function.
 */
class CodeGenContext {
  import CodeGenTypes._

  /**
   * Holding a list of objects that could be used passed into generated class.
   */
  val references: mutable.ArrayBuffer[Any] = new mutable.ArrayBuffer[Any]()

  /**
   * Add an object to `references`, create a class member to access it.
   *
   * Returns the name of class member.
   */
  def addReferenceObj(name: String, obj: Any, className: String = null): String = {
    val term = freshName(name)
    val idx = references.length
    references += obj
    val clsName = Option(className).getOrElse(obj.getClass.getName)
    addMutableState(clsName, term, s"this.$term = ($clsName) references[$idx];")
    term
  }

  /**
   * Holding  mutable states like `MonotonicallyIncreasingID.count` as a
   * 3-tuple: java type, variable name, code to init it.
   * As an example, ("int", "count", "count = 0;") will produce code:
   * {{{
   *   private int count;
   * }}}
   * as a member variable, and add
   * {{{
   *   count = 0;
   * }}}
   * to the constructor.
   *
   * They will be kept as member variables in generated classes
   */
  private val mutableStates: mutable.ArrayBuffer[(String, String, String)] =
    mutable.ArrayBuffer.empty[(String, String, String)]

  def addMutableState(javaType: String, variableName: String, initCode: String): Unit = {
    mutableStates += ((javaType, variableName, initCode))
  }

  def declareMutableStates(): String = {
    // It's possible that we add same mutable state twice. 
    // Call `distinct` here to remove the duplicated ones.
    mutableStates.distinct.map { case (javaType, variableName, _) =>
      s"private $javaType $variableName;"
    }.mkString("\n")
  }

  def initMutableStates(): String = {
    // It's possible that we add same mutable state twice. 
    // Call `distinct` here to remove the duplicated ones.
    mutableStates.distinct.map(_._3).mkString("\n")
  }

  /**
   * Map of all functions to be added to generated class
   */
  private val addedFunctions: mutable.Map[String, String] =
    mutable.Map.empty[String, String]

  def addNewFunction(funcName: String, funcCode: String): Unit = {
    addedFunctions += ((funcName, funcCode))
  }

  def declareAddedFunctions(): String = {
    addedFunctions.map { case (funcName, funcCode) => funcCode }.mkString("\n")
  }

  /**
   * The map from a place holder to a corresponding comment
   */
  private val placeHolderToComments = new mutable.HashMap[String, String]

  /**
   * The map from a variable name to it's next ID.
   */
  private val freshNameIds = new mutable.HashMap[String, Int]

  /**
   * A prefix used to generate fresh name.
   */
  var freshNamePrefix = ""

  /**
   * Returns a term name that is unique within this instance of a `CodegenContext`.
   */
  def freshName(name: String): String = synchronized {
    val fullName = if (freshNamePrefix == "") {
      name
    } else {
      s"${freshNamePrefix}_$name"
    }
    if (freshNameIds.contains(fullName)) {
      val id = freshNameIds(fullName)
      freshNameIds(fullName) = id + 1
      s"$fullName$id"
    } else {
      freshNameIds += fullName -> 1
      fullName
    }
  }

  /**
   * Returns the specialized code to access a value from 'input' at `ordinal`.
   */
  def getValue(input: String, javaType: String, ordinal: String): String = {
    javaType match {
      case JAVA_NULL => "null"
      case _ if isPrimitiveType(javaType) => s"$input"
      case JAVA_MAP => s"$input.get($ordinal)"
      case JAVA_ARRAY => s"$input.get($ordinal)"
      case _ =>
        throw new IllegalArgumentException(s"cannot generate code for unsupported type: $javaType")
    }
  }

  /**
   * Returns the specialized code to set a given value in 'output; at `ordinal`.
   */
  def setValue(output: String, javaType: String, ordinal: Int, value: String): String = {
    javaType match {
      case JAVA_NULL => s"$javaType output = null"
      case _ if isPrimitiveType(javaType) => s"$javaType $output = $value"
      case JAVA_MAP => s"$output.set($ordinal, $value)"
      case JAVA_ARRAY => s"$output.set($ordinal, $value)"
      case _ =>
        throw new IllegalArgumentException(s"cannot generate code for unsupported type: $javaType")
    }
  }

  /**
   * Returns the representation of default value for a given Java Type.
   */
  def defaultValue(javaType: String): String = javaType match {
    case JAVA_BOOLEAN => "false"
    case JAVA_BYTE => "(byte)-1"
    case JAVA_SHORT => "(short)-1"
    case JAVA_INT => "-1"
    case JAVA_LONG => "-1L"
    case JAVA_FLOAT => "-1.0f"
    case JAVA_DOUBLE => "-1.0"
    case _ => "null"
  }

  /**
   * Generates code for equal expression in Java.
   */
  def genEqual(javaType: String, c1: String, c2: String): String = javaType match {
    case JAVA_FLOAT => s"(java.lang.Float.isNaN($c1) && java.lang.Float.isNaN($c2)) || $c1 == $c2"
    case JAVA_DOUBLE => s"(java.lang.Double.isNaN($c1) && java.lang.Double.isNaN($c2)) || $c1 == $c2"
    case _ if isPrimitiveType(javaType) => s"$c1 == $c2"    
    case other => s"$c1.equals($c2)"
  }

  /**
   * Generates code for comparing two expressions.
   *
   * @param javaType data type of the expressions
   * @param c1 name of the variable of expression 1's output
   * @param c2 name of the variable of expression 2's output
   */
  def genComp(javaType: String, c1: String, c2: String): String = javaType match {
    case JAVA_NULL => "0"
    // java boolean doesn't support > or < operator
    case JAVA_BOOLEAN => s"($c1 == $c2 ? 0 : ($c1 ? 1 : -1))"
    // TODO:  c1 - c2 may overflow
    case _ if isPrimitiveType(javaType) => s"($c1 > $c2 ? 1 : $c1 < $c2 ? -1 : 0)"
    case _ =>
      throw new IllegalArgumentException("cannot generate compare code for type")
  }

  /**
   * Generates code for greater of two expressions.
   *
   * @param javaType data type of the expressions
   * @param c1 name of the variable of expression 1's output
   * @param c2 name of the variable of expression 2's output
   */
  def genGreater(javaType: String, c1: String, c2: String): String = javaType match {
    case JAVA_BYTE | JAVA_SHORT | JAVA_INT | JAVA_LONG => s"$c1 > $c2"
    case _ => s"(${genComp(javaType, c1, c2)}) > 0"
  }

  /**
   * Generates code to do null safe execution, i.e. only execute the code when the input is not
   * null by adding null check if necessary.
   *
   * @param nullable used to decide whether we should add null check or not.
   * @param isNull the code to check if the input is null.
   * @param execute the code that should only be executed when the input is not null.
   */
  def nullSafeExec(nullable: Boolean, isNull: String)(execute: String): String = {
    if (nullable) {
      s"""
        if (!$isNull) {
          $execute
        }
      """
    } else {
      "\n" + execute
    }
  }

  /**
   * List of java data types
   */
  val primitiveTypes =
    Seq(JAVA_BOOLEAN, JAVA_BYTE, JAVA_SHORT, JAVA_INT, JAVA_LONG, JAVA_FLOAT, JAVA_DOUBLE)

  /**
   * Returns true if primitive java type 
   */
  def isPrimitiveType(jt: String): Boolean = primitiveTypes.contains(jt)
  
  /**
   * Splits the generated code of expressions into multiple functions, because function has
   * 64kb code size limit in JVM
   *
   * @param row the variable name of row that is used by expressions
   * @param expressions the codes to evaluate expressions.
   */
  def splitExpressions(row: String, expressions: Seq[String]): String = {
    if (row == null) {
      // Cannot split these expressions because they are not created from a row object.
      return expressions.mkString("\n")
    }
    val blocks = new ArrayBuffer[String]()
    val blockBuilder = new StringBuilder()
    for (code <- expressions) {
      // We can't know how many byte code will be generated, so use the number of bytes as limit
      if (blockBuilder.length > 64 * 1000) {
        blocks.append(blockBuilder.toString())
        blockBuilder.clear()
      }
      blockBuilder.append(code)
    }
    blocks.append(blockBuilder.toString())

    if (blocks.length == 1) {
      // inline execution if only one block
      blocks.head
    } else {
      val apply = freshName("apply")
      val functions = blocks.zipWithIndex.map { case (body, i) =>
        val name = s"${apply}_$i"
        val code = s"""
           |private void $name(InternalRow $row) {
           |  $body
           |}
         """.stripMargin
        addNewFunction(name, code)
        name
      }

      functions.map(name => s"$name($row);").mkString("\n")
    }
  }  

  /**
   * get a map of the pair of a place holder and a corresponding comment
   */
  def getPlaceHolderToComments(): collection.Map[String, String] = placeHolderToComments

  /**
   * Register a comment and return the corresponding place holder
   */
  def registerComment(text: => String): String = {
    // TODO:  By default, disable comments in generated code because computing the comments themselves can
    // be extremely expensive in certain cases, such as deeply-nested expressions which operate over
    // inputs with wide schemas. For more details on the performance issues that motivated this
    // flat, see SPARK-15680.
    val name = freshName("c")
    val comment = if (text.contains("\n") || text.contains("\r")) {
      text.split("(\r\n)|\r|\n").mkString("/**\n * ", "\n * ", "\n */")
    } else {
      s"// $text"
    }
    placeHolderToComments += (name -> comment)
    s"/*$name*/"
  }
}

/**
 * A wrapper for generated class, defines a `generate` method so that we can pass extra objects
 * into generated class.
 */
abstract class GeneratedClass {
  def generate(references: Array[Any]): Any
}

/**
 * A wrapper for the source code to be compiled by [[CodeGenerator]].
 */
class CodeAndComment(val packageName: String, val body: String, val comment: collection.Map[String, String])
  extends Serializable {
  override def equals(that: Any): Boolean = that match {
    case t: CodeAndComment if (t.body == body && t.packageName == packageName) => true
    case _ => false
  }

  override def hashCode(): Int = body.hashCode
}

/**
 * A base class for generators of byte code to perform expression evaluation.  Includes a set of
 * helpers for referring to Catalyst types and building trees that perform evaluation of individual
 * expressions.
 */
abstract class CodeGenerator[InType <: AnyRef, OutType <: AnyRef] {

  /**
   * Generates a class for a given input expression.  
   * Called if code is not already cached.
   */
  protected def create(in: InType): OutType

  /**
   * Canonicalizes an input expression. Used to avoid double caching expressions that differ only
   * cosmetically.
   */
  protected def canonicalize(in: InType): InType

  /**
   * Create a new codegen context for expression evaluator, used to store those
   * expressions that don't support codegen
   */
  def newCodeGenContext(): CodeGenContext = {
    new CodeGenContext
  }
}

object CodeGenerator {
  
  /**
   * Compile the Java source code into a Java class, using Janino.
   */
  def compile(code: CodeAndComment): GeneratedClass = {
    cache.get(code)
  }

  /**
   * Compile the Java source code into a Java class, using Janino.
   */
  private[this] def doCompile(code: CodeAndComment): GeneratedClass = {
    val evaluator = new ClassBodyEvaluator()

    // A special classloader used to wrap the actual parent classloader of
    // [[org.codehaus.janino.ClassBodyEvaluator]] (see CodeGenerator.doCompile). This classloader
    // does not throw a ClassNotFoundException with a cause set (i.e. exception.getCause returns
    // a null). This classloader is needed because janino will throw the exception directly if
    // the parent classloader throws a ClassNotFoundException with cause set instead of trying to
    // find other possible classes (see org.codehaus.janinoClassLoaderIClassLoader's
    // findIClass method). Please also see https://issues.apache.org/jira/browse/SPARK-15622 and
    // https://issues.apache.org/jira/browse/SPARK-11636.
    val parentClassLoader = new ParentClassLoader(CodeGenUtils.getContextOrSparkClassLoader)
    evaluator.setParentClassLoader(parentClassLoader)
    // Cannot be under package codegen, or fail with java.lang.InstantiationException
    evaluator.setClassName(code.packageName)
    evaluator.setDefaultImports(Array(
      "java.util.Arrays",
      "java.util.Set",
      "java.util.HashSet",
      "java.util.Map",
      "java.util.HashMap",
      "org.apache.spark.ml.linalg.Vectors",
      "org.apache.spark.ml.linalg.Vector"
    ))
    evaluator.setExtendedClass(classOf[GeneratedClass])

    lazy val formatted = CodeFormatter.format(code)

    // TODO:  Only add extra debugging info to byte code when we are going to print the source code.
      evaluator.setDebuggingInformation(true, true, false)
      s"\n$formatted"
          
    try {
      evaluator.cook("generated.java", code.body)
      recordCompilationStats(evaluator)
    } catch {
      case e: Exception =>
        val msg = s"failed to compile: $e\n$formatted"
        System.out.println(msg, e)
        throw new Exception(msg, e)
    }
    evaluator.getClazz().newInstance().asInstanceOf[GeneratedClass]
  }

  /**
   * Records the generated class and method bytecode sizes by inspecting janino private fields.
   */
  private def recordCompilationStats(evaluator: ClassBodyEvaluator): Unit = {
    // First retrieve the generated classes.
    val classes = {
      val resultField = classOf[SimpleCompiler].getDeclaredField("result")
      resultField.setAccessible(true)
      val loader = resultField.get(evaluator).asInstanceOf[ByteArrayClassLoader]
      val classesField = loader.getClass.getDeclaredField("classes")
      classesField.setAccessible(true)
      classesField.get(loader).asInstanceOf[JavaMap[String, Array[Byte]]].asScala
    }

    // Then walk the classes to get at the method bytecode.
    val codeAttr = CodeGenUtils.classForName("org.codehaus.janino.util.ClassFile$CodeAttribute")
    val codeAttrField = codeAttr.getDeclaredField("code")
    codeAttrField.setAccessible(true)
    classes.foreach { case (_, classBytes) =>
      CodeGenMetrics.METRIC_GENERATED_CLASS_BYTECODE_SIZE.update(classBytes.length)
      val cf = new ClassFile(new ByteArrayInputStream(classBytes))
      cf.methodInfos.asScala.foreach { method =>
        method.getAttributes().foreach { a =>
          if (a.getClass.getName == codeAttr.getName) {
            CodeGenMetrics.METRIC_GENERATED_METHOD_BYTECODE_SIZE.update(
              codeAttrField.get(a).asInstanceOf[Array[Byte]].length)
          }
        }
      }
    }
  }

  /**
   * A cache of generated classes.
   *
   * From the Guava Docs: A Cache is similar to ConcurrentMap, but not quite the same. The most
   * fundamental difference is that a ConcurrentMap persists all elements that are added to it until
   * they are explicitly removed. A Cache on the other hand is generally configured to evict entries
   * automatically, in order to constrain its memory footprint.  Note that this cache does not use
   * weak keys/values and thus does not respond to memory pressure.
   */
  private val cache = CacheBuilder.newBuilder()
    .maximumSize(100)
    .build(
      new CacheLoader[CodeAndComment, GeneratedClass]() {
        override def load(code: CodeAndComment): GeneratedClass = {
          val startTime = System.nanoTime()
          val result = doCompile(code)
          val endTime = System.nanoTime()
          def timeMs: Double = (endTime - startTime).toDouble / 1000000
          CodeGenMetrics.METRIC_SOURCE_CODE_SIZE.update(code.body.length)
          CodeGenMetrics.METRIC_COMPILATION_TIME.update(timeMs.toLong)
          System.out.println(s"Code generated in $timeMs ms")
          result
        }
      })
}
