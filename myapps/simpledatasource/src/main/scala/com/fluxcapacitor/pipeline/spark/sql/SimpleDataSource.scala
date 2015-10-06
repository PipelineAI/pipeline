package com.fluxcapacitor.pipeline.spark.sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.sources.{RelationProvider, BaseRelation, TableScan}
import org.apache.spark.sql.types.{StructType, StructField, DoubleType}
import scala.util.Random

case class RandomGaussianRelation(numSamples: Int, seed: Long)
    (@transient val sqlContext: SQLContext) 
    extends BaseRelation with TableScan {
  val random = new Random(seed)
  override def schema = StructType(
    StructField("rqndom_gaussian_double", DoubleType, nullable = false) :: Nil)
  override def buildScan() = sqlContext.sparkContext
    .parallelize(0 to numSamples).map(i => Row(random.nextGaussian()))
}

class RandomGaussianRelationProvider extends RelationProvider {
  override def createRelation(sqlContext: SQLContext, 
      parameters: Map[String, String]): BaseRelation = {
    RandomGaussianRelation(
      parameters("numSamples").toInt, parameters("seed").toLong)(sqlContext)
  }
}
