package ai.pipeline.predict.jvm

trait Predictable {
  def predict(inputs:java.util.Map[String,Any]): Any
}
