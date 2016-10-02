package com.advancedspark.pmml.spark.ml

import java.io.File

import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.Predictor
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.DataFrameReader
import org.apache.spark.sql.types.StructType
import org.dmg.pmml.FieldName
import org.dmg.pmml.DataField
import org.xml.sax.InputSource

import org.apache.commons.io.IOUtils
import java.net.URL
import java.nio.charset.Charset
import java.io.File

object PostPMML {
  /*
    url: http://127.0.0.1:9040/update-pmml
    pmmlName: census
  */
  def post(pmmlPath: String, pmmlName: String, postUrl: String) {
    import org.apache.http.client.methods.HttpPost
    import org.apache.http.entity.StringEntity
    import org.apache.http.impl.client.DefaultHttpClient // TODO:  this is deprecated

    // create an HttpPost object
    println("--- HTTP POST PMML ---")
    val post = new HttpPost(s"${postUrl}/${pmmlName}")

    // set the Content-type
    post.setHeader("Content-type", "application/xml")

    // add the JSON as a StringEntity
    val is = new java.io.FileInputStream(pmmlPath)
    val pmml = IOUtils.toString(is, "utf8")
    post.setEntity(new StringEntity(pmml))

    // send the post request
    val response = (new DefaultHttpClient).execute(post)

    // print the response headers
    println("--- HTTP RESPONSE HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
  }
}
