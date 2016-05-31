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

package com.advancedspark.codegen.spark.ml.recommendation

import org.codehaus.janino.ClassBodyEvaluator

import org.apache.spark.ml.tree.Node
import org.apache.spark.ml.tree.CategoricalSplit
import org.apache.spark.ml.tree.InternalNode
import org.apache.spark.ml.tree.ContinuousSplit
import org.apache.spark.ml.tree.LeafNode
import org.apache.spark.mllib.linalg.Vector

trait CallableVectorDouble {
  def apply(v: Vector): Double
}

object ALSModelGenerator {
  private val classNamePrefix = "/1.6.1/spark/ml/recommendation/als"
  private val currentClassNameId = new java.util.concurrent.atomic.AtomicInteger()
  private val currentMethodNameId = new java.util.concurrent.atomic.AtomicInteger()

  /**
   * Compile the generated source code using Janino
   * @param generated source code
   * @param interfaces used by the generated source code
   */
  protected def compile(generatedSourceCode: String, interfaces: Array[Class[_]]): Class[_] = {
    val startTime = System.nanoTime()
 
    val evaluator = new ClassBodyEvaluator()
 
    // TODO:  Put each model in their own classloader so we can tear them down cleanly
    evaluator.setParentClassLoader(getClass.getClassLoader())

    evaluator.setImplementedInterfaces(interfaces)

    val generatedClassName = freshClassName()

    evaluator.setClassName(generatedClassName)

    evaluator.setDefaultImports(Array(
      "org.apache.spark.mllib.linalg.Vectors",
      "org.apache.spark.mllib.linalg.Vector",
      "java.util.Arrays",
      "java.util.HashSet"
    ))

    // Compile the code
    evaluator.cook(s"${generatedClassName}.java", generatedSourceCode)
    
    val endTime = System.nanoTime()
  
    def timeMs: Double = (endTime - startTime).toDouble / 1000000
    System.out.println(s"Compiled Java code (${generatedSourceCode.size} bytes) in $timeMs ms")

    // Return the generated class file
    evaluator.getClazz()
  }

  protected def freshClassName(): String = {
    s"${classNamePrefix}${currentClassNameId.getAndIncrement}"
  }

  protected def freshMethodName(): String = {
    s"${currentMethodNameId.getAndIncrement}"
  }

  /**
   * Returns a string for the current function body
   * TODO: Implement the dot product using new local linear algebra libs 
   */
  def generateSourceCode(name: String): String = {
    val code = ""

    s"""
      public double ${name}(Vector factors1, Vector factors2) throws Exception {
        ${code}
      }
    """
  }

  def getScorer(root: Node): CallableVectorDouble = {
    val code =
      s"""
       @Override
       ${generateSourceCode("apply")}
       """
    val jfunc = compile(code,
      Array(classOf[Serializable], classOf[CallableVectorDouble])).newInstance()
    jfunc.asInstanceOf[CallableVectorDouble]
  }
}
