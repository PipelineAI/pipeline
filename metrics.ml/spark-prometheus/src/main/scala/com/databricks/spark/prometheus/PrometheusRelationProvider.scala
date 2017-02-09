/*
 * Copyright 2014 Databricks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.databricks.spark.prometheus

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, DataSourceRegister, RelationProvider, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType, TimestampType}


class PrometheusRelationProvider extends RelationProvider with DataSourceRegister {

  override def createRelation(ctx: SQLContext, parameters: Map[String, String])
    : BaseRelation with TableScan = new BaseRelation with TableScan {

    val endpoint = parameters("endpoint")
    val query = parameters.getOrElse("path", parameters("query"))
    val startMs = parameters("start").toLong
    val endMs = parameters("end").toLong
    val stepMs = parameters("step").toLong

    val entries = PrometheusClient.read(endpoint, query, startMs, endMs, stepMs)

    override def sqlContext: SQLContext = ctx

    override def schema: StructType = {
      val labels = entries.headOption.map { entry =>
        StructType(entry.labels.map(x => StructField(x._1, StringType, nullable = true)))
      }.getOrElse {
        new StructType()
      }

      labels
        .add("time", TimestampType)
        .add("value", StringType)
    }

    override def buildScan(): RDD[Row] = {
      if (entries.isEmpty) {
        ctx.sparkContext.emptyRDD
      } else {
        val rows = entries.flatMap { entry =>
          val prefix: Seq[String] = entry.labels.map(_._2)
          entry.values.map { case (time, value) =>
            val convertedTime = new Timestamp(time)
            Row.fromSeq(prefix :+ convertedTime :+ value)
          }
        }
        ctx.sparkContext.parallelize(rows)
      }
    }
  }

  override def shortName(): String = "prometheus"
}
