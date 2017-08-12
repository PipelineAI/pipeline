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

import org.scalatest.FunSuite

import org.apache.spark.sql.SparkSession

class PrometheusSourceSuite extends FunSuite {

  ignore("test") {
    val spark = SparkSession.builder().master("local").getOrCreate()

    val df = spark.read.format("prometheus")
      .option("start", (1472182980.739 * 1000).toLong)
      .option("end", (1472186580.739 * 1000).toLong)
      .option("step", 14 * 1000)
      .option("endpoint", "http://some-host")
      .load("query")

    df.show(100, false)

    spark.stop()
  }
}
