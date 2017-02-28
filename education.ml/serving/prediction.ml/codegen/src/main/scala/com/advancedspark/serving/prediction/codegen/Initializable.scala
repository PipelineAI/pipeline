package com.advancedspark.serving.prediction.codegen

import scala.collection.JavaConverters.mapAsJavaMapConverter

trait Initializable {
  def initialize(args: java.util.Map[String, Any]): Unit
}
