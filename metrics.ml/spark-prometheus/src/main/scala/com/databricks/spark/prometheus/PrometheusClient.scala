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

import java.net.URLEncoder
import java.util.{List => JList, Map => JMap}

import scala.collection.JavaConverters._

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory

/**
 * A HTTP client for the [[https://prometheus.io/docs/querying/api/ Prometheus V1 HTTP API]].
 *
 */
object PrometheusClient {

  private val log = LoggerFactory.getLogger(getClass)

  class PrometheusResult(
      val labels: Seq[(String, String)],
      val values: Seq[(Long, String)])

  /**
   * @param endpoint URL to the root of the Prometheus web UI, e.g. "http://prom.mydomain.com:1234"
   * @param query query to pass to Prometheus
   */
  def read(
      endpoint: String,
      query: String,
      startTimeMs: Long,
      endTimeMs: Long,
      stepMs: Long): Seq[PrometheusResult] = {

    val encodedQuery = URLEncoder.encode(query, "UTF8")
    val start = BigDecimal(startTimeMs) / 1000
    val end = BigDecimal(endTimeMs) / 1000
    val step = BigDecimal(stepMs) / 1000

    val url = s"$endpoint/api/v1/query_range?query=$encodedQuery&start=$start&end=$end&step=$step"

    val startTime = System.nanoTime()
    val json: String = scala.io.Source.fromURL(url).mkString
    val timeTaken = (System.nanoTime() - startTime) / 1000 / 1000
    log.debug(s"Request took $timeTaken ms: " + url)

    // This is pretty hacky, but it is a pain to work with JSON in a type safe language
    // where the data is actually dynamically typed ...
    val map = new ObjectMapper().readValue(json, classOf[JMap[String, Object]])

    val data = map.get("data").asInstanceOf[JMap[String, Object]]
    val result = data.get("result").asInstanceOf[JList[JMap[String, Object]]]

    result.asScala.map { item =>
      val labels = item.get("metric").asInstanceOf[JMap[String, String]].asScala.toSeq
      val values = item.get("values").asInstanceOf[JList[JList[Object]]].asScala.map { v =>
        ((v.get(0).asInstanceOf[Double] * 1000).toLong, v.get(1).asInstanceOf[String])
      }
      new PrometheusResult(labels, values)
    }
  }
}
