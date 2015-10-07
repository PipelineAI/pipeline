package com.fluxcapacitor.pipeline.spark.sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.sources.{RelationProvider, BaseRelation, TableScan}
import org.apache.spark.sql.types.{StructType, StructField, IntegerType}

case class IntegerRangeRelation(start: Int, end: Int)
    (@transient val sqlContext: SQLContext) 
    extends BaseRelation with TableScan {
  override def schema = StructType(
    StructField("int_column", IntegerType, nullable = false) :: Nil)
  override def buildScan() = sqlContext.sparkContext
    .parallelize(start to end).map(int => Row(int))
}

class IntegerRangeRelationProvider extends RelationProvider {
  override def createRelation(sqlContext: SQLContext, 
      parameters: Map[String, String]): BaseRelation = {
    IntegerRangeRelation(
      parameters("start").toInt, parameters("end").toInt)(sqlContext)
  }
}
