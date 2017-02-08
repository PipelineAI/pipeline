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

import com.databricks.spark.prometheus.PrometheusClient.PrometheusResult


class PrometheusClientSuite extends FunSuite {

  ignore("test") {
    val ret: Seq[PrometheusResult] = PrometheusClient.read(
      "http://some-host",
      "query",
      (1472182980.739 * 1000).toLong,
      (1472186580.739 * 1000).toLong,
      14 * 1000)

    println(s"found ${ret.size} results")

    ret.foreach { m: PrometheusResult =>
      m.labels.foreach(println)
      m.values.foreach(println)
    }
  }
}
