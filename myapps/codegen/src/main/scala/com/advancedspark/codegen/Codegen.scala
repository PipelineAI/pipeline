package com.advancedspark.codegen

import org.codehaus.janino._;

object Codegen {
  def main(args: Array[String]) {
    val ee = new ExpressionEvaluator();
    ee.cook("3 + 4");
    System.out.println(ee.evaluate(null));
  }
}
