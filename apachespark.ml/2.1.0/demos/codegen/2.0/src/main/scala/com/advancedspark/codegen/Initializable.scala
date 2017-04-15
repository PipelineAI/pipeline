package com.advancedspark.codegen

trait Initializable {
  def initialize(references: Array[Any]): Unit
}
