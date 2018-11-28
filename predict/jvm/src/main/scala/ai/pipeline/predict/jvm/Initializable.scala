package ai.pipeline.predict.jvm

trait Initializable {
  def initialize(args: java.util.Map[String, Any]): Unit
}
