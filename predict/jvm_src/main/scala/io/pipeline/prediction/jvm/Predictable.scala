package io.pipeline.prediction.jvm

trait Predictable {
  def predict(inputs:java.util.Map[String,Any]): Any
}
