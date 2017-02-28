package com.advancedspark.serving.prediction.codegen

import scala.collection.JavaConverters.mapAsJavaMapConverter

trait Predictable {
  def predict(inputs:java.util.Map[String,Any]): Any
}
