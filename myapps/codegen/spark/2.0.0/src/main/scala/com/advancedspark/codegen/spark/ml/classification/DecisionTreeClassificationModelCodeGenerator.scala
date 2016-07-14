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
package com.advancedspark.codegen.spark.ml.classification 

import org.codehaus.janino.ClassBodyEvaluator

import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.tree.Node
import org.apache.spark.ml.tree.CategoricalSplit
import org.apache.spark.ml.tree.InternalNode
import org.apache.spark.ml.tree.ContinuousSplit
import org.apache.spark.ml.tree.LeafNode
import org.apache.spark.ml.linalg.Vector

trait CallableVectorDouble {
  def apply(v: Vector): Double
}

object DecisionTreeClassificationModelCodeGenerator { 
  private val classNamePrefix = "/2.0.0/spark/ml/classification/GeneratedDecisionTreeClassificationModel"
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
      "org.apache.spark.ml.linalg.Vectors",
      "org.apache.spark.ml.linalg.Vector",
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
   * Convert the tree starting at the provided root node into a code generated
   * series of if/else statements. If the tree is too large to fit in a single
   * in-line method breaks it up into multiple methods.
   * Returns a string for the current function body and a string of any additional
   * functions.
   */
  def generateSourceCode(root: Node, depth: Int): (String, String) = {
/*
    // Generate the conditional for provide categories
    def categoryMatchConditional(split: CategoricalSplit) = {
      val allCategories = split.leftCategories ++ split.rightCategories

      if (allCategories.size < 64) {
        def generateCondition(categoryValue: Double) = {
          s"${categoryValue} == fValue"
        }
        s"""
        Double fValue = input.apply(${split.featureIndex});
        if (${allCategories.map(generateCondition).mkString(" || ")}) {
        """
      } else {
        s"""
        HashSet<Double> categories = new HashSet(Arrays.asList(${allCategories.mkString(" ,")}));
        if (categories.contains(input.apply(${split.featureIndex}))) {
        """
      }
    }
  
    // Handle the different types of nodes
    root match {
      case node: InternalNode =>
        // Handle trees that get too large to fit in a single in-line java method
        depth match {
          case 8 =>
            val newMethodName = freshMethodName()
            val newMethod = generateSourceCode(root, newMethodName)
            (s"return ${newMethodName}(input);", newMethod)
          case _ =>
            val nodeSplit = node.split
            val (leftSubCode, leftSubFunction) = generateSourceCode(node.leftChild, depth + 1)
            val (rightSubCode, rightSubFunction) = generateSourceCode(node.rightChild, depth + 1)
            val subCode = nodeSplit match {
              case split: CategoricalSplit =>
                //val isLeft = split.isLeft
		val allCategories = split.leftCategories ++ split.rightCategories
 		val isLeft: Boolean = split.leftCategories.length <= (allCategories.size / 2)
                isLeft match {
                  case true => s"""
                              ${categoryMatchConditional(split)}
                                ${leftSubCode}
                              } else {
                                ${rightSubCode}
                              }"""
                  case false => s"""
                               ${categoryMatchConditional(split)}
                                 ${rightSubCode}
                               } else {
                                 ${leftSubCode}
                               }"""
                }
              case split: ContinuousSplit =>
                s"""
               if (input.apply(${split.featureIndex}) <= ${split.threshold}) {
                 ${leftSubCode}
                } else {
                 ${rightSubCode}
                }"""
            }
            (subCode, leftSubFunction + rightSubFunction)
        }
      case node: LeafNode => (s"return ${node.prediction};", "")
    }
*/
    // return empty strings for now
    ("","")
  }

  /**
   * Convert the tree starting at the provided root node into a code generated
   * series of if/else statements. If the tree is too large to fit in a single
   * in-line method breaks it up into multiple methods.
   */
  def generateSourceCode(node: Node, name: String): String = {
    val (code, extraFunctions) = generateSourceCode(node, 0)

    s"""
     public double ${name}(Vector input) throws Exception {
       ${code}
     }

     ${extraFunctions}
     """
  }

  // Create a codegened scorer for a given node
  def getScorer(root: Node): CallableVectorDouble = {
    val code =
      s"""
       @Override
       ${generateSourceCode(root, "apply")}
       """
    val jfunc = compile(code,
      Array(classOf[Serializable], classOf[CallableVectorDouble])).newInstance()
    jfunc.asInstanceOf[CallableVectorDouble]
  }
}
