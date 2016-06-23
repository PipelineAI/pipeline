/**
package com.advancedspark.serving.prediction.util

import breeze.linalg.DenseVector
import java.io.Serializable
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read=>jread, write=>jwrite}
import org.json4s.NoTypeHints
import scala.util.Try

case class FrozenALSModelJson(userFactors: Array[Double], itemFactors: Array[Double], metadata: Map[Any, Any])

// TODO
class ALSModelJson(var userFactors: DenseVector[Double], var itemFactors: DenseVector[Double], var metadata: Map[Any, Any])
  extends Serializable {

  def freeze: FrozenALSModelJson = {
    // TODO
    null 
  }
}

object ALSModelJsonSerializer {
  implicit val format = Serialization.formats(NoTypeHints)

  def toJson(model: ALSModelJson): String = {
    jwrite(model.freeze)
  }

  def fromJson(json: String): Try[ALSModelJson] = {
    Try({
      thaw(jread[FrozenALSModelJson](json))
    })
  }

  def thaw(frozenModel: FrozenALSModelJson): ALSModelJson = {
    // TODO
    null
  }
}
**/
