package com.fluxcapacitor.pipeline.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.sources.{RelationProvider, SchemaRelationProvider, CreatableRelationProvider, BaseRelation}
import org.apache.spark.sql.sources.{PrunedFilteredScan, InsertableRelation}
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.{StructType, StructField, IntegerType}

case class IntegerRangeRelation(start: Int, end: Int)(@transient val sqlContext: SQLContext)
  extends BaseRelation with PrunedFilteredScan with InsertableRelation {

  override def schema = StructType(
    StructField("int_col", IntegerType, nullable = false) :: Nil)

  val startingValues = (start to end)
  val inserts = scala.collection.mutable.MutableList[Int]() 

  // PrunedFilteredScan impla
  // Note:  This is merely an optimization.
  //        Spark SQL will re-apply the Filters to ensure nothing slips by.
  override def buildScan(requiredColumns: Array[String], filter: Array[Filter]): RDD[Row] = {
    // TODO:  Return only the columns specified
    // TODO:  Apply the Filters
    
    val values = startingValues ++ inserts 
    sqlContext.sparkContext.parallelize(values).map(Row(_))
  }

  // InsertableRelation impl
  override def insert(df: DataFrame, overwrite: Boolean): Unit = {
    inserts ++= df.collect().map(row => row.getInt(0)) 
  }
}

class IntegerRangeRelationProvider extends RelationProvider with SchemaRelationProvider with CreatableRelationProvider {
  // RelationProvider impl
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    IntegerRangeRelation(parameters("start").toInt, parameters("end").toInt)(sqlContext)
  }

  // SchemaRelationProvider impl
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String], schema: StructType): BaseRelation = {
    // TODO:  Handle schema
    IntegerRangeRelation(parameters("start").toInt, parameters("end").toInt)(sqlContext)
  }

  // CreatableRelationProvider impl
  override def createRelation(sqlContext: SQLContext, mode: SaveMode, parameters: Map[String, String], data: DataFrame): BaseRelation =  {
    // TODO:  Handle mode
    // TODO:  Handle data
    IntegerRangeRelation(parameters("start").toInt, parameters("end").toInt)(sqlContext)
  }
}
