package io.pipeline.prediction.jvm

trait Initializable {
  def initialize(args: java.util.Map[String, Any]): Unit
}
