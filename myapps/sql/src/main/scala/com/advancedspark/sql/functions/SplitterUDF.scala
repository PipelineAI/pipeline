package com.advancedspark.sql.functions

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.catalyst.expressions.{Expression, BinaryExpression}
import org.apache.spark.sql.sources.{RelationProvider, SchemaRelationProvider, CreatableRelationProvider, BaseRelation}
import org.apache.spark.sql.sources.{PrunedFilteredScan, InsertableRelation}
import org.apache.spark.sql.sources.{Filter, LessThan, GreaterThan}
import org.apache.spark.sql.catalyst.expressions.ExpectsInputTypes
import org.apache.spark.sql.types._
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.codegen.CodeGenContext
import org.apache.spark.sql.catalyst.expressions.codegen.GeneratedExpressionCode
import org.apache.spark.sql.catalyst.analysis.UnresolvedException
import org.apache.spark.sql.catalyst.expressions.codegen._
import org.apache.spark.unsafe.types.UTF8String
import org.apache.spark.sql.catalyst.expressions.ImplicitCastInputTypes
import org.apache.spark.sql.catalyst.expressions.TernaryExpression
 
/**
 * Return a list of tokens separated by the given separator
 * @param string:  string to split
 * @param delimiter:  delimiter string
 * @param limit:  See java.util.String docs for more details
 */
case class SplitterUDF(string: Expression, delimiter: Expression, limit: Expression) 
  extends TernaryExpression with ImplicitCastInputTypes {
  
  override def dataType: ArrayType = ArrayType(StringType)
  override def inputTypes: Seq[DataType] = Seq(StringType, StringType, IntegerType)
  override def children: Seq[Expression] = Seq(string, delimiter, limit)
  override def prettyName = "splitter"

  
  override def nullSafeEval(string: Any, delimiter: Any, limit: Any): Any = {
    string.asInstanceOf[UTF8String].split(
      delimiter.asInstanceOf[UTF8String], limit.asInstanceOf[Int]
    )
  }

  override def genCode(ctx: CodeGenContext, ev: GeneratedExpressionCode): String = {
    defineCodeGen(ctx, ev, (string, delimiter, limit) => 
      s"$string.split($delimiter, $limit)"
    )
  }
}
