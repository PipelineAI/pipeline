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

/*
age,workclass,education,education_num,marital_status,occupation,relationship,race,sex,capital_gain,capital_loss,hours_per_week,native_country,income
*/
case class Census(
  age: Integer, workclass: String, education: String, education_num: Integer, marital_status: String,
  occupation: String, relationship: String, race: String, sex: String, capital_gain: Integer,
  capital_loss: Integer, hours_per_week: Integer, native_country: String, income: String
)

object GeneratePMML {
  def main(args: Array[String]) = {
    val sparkConf: SparkConf = new SparkConf()
    val sc: SparkContext = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
  
    val datasetUrl = "https://raw.githubusercontent.com/fluxcapacitor/datapalooza.ml/master/R/census.csv"
    val datasetLines = IOUtils.toString(new URL(datasetUrl), "utf8")
    val datasetRDD = sc.parallelize(datasetLines.split("\n"))

    import sqlContext.implicits._

    val censusDF = datasetRDD.map(s => s.split(","))
      .filter(s => s(0) != "age").map(s => 
        Census(s(0).toInt, 
              s(1),
              s(2),
              s(3).toInt,
              s(4),
              s(5),
              s(6),
              s(7),
              s(8),
              s(9).toInt,
              s(10).toInt,
              s(11).toInt,
              s(12),
              s(13))
       ).toDF()

    import org.apache.spark.ml.classification.DecisionTreeClassificationModel

    val formulaStr: String = "income ~ ."

    val schema: StructType = censusDF.schema

    System.out.println(schema.treeString)

    val formula: RFormula = new RFormula().setFormula(formulaStr)

    var predictor: Predictor[_, _, _] = new DecisionTreeClassifier().setMinInstancesPerNode(10)

    predictor.setLabelCol(formula.getLabelCol)
    predictor.setFeaturesCol(formula.getFeaturesCol)

    val pipeline = new Pipeline().setStages(Array[PipelineStage](formula, predictor))

    val pipelineModel = pipeline.fit(censusDF)

    val predictorModel = pipelineModel.stages(1).asInstanceOf[DecisionTreeClassificationModel]

    System.out.println(predictorModel.toDebugString)

    import org.jpmml.sparkml.ConverterUtil

    val pmml = ConverterUtil.toPMML(schema, pipelineModel)
    System.out.println(pmml.getModels().get(0).toString())

    import org.jpmml.model.ImportFilter
    import org.jpmml.model.JAXBUtil
    import org.jpmml.model.MetroJAXBUtil

    val pmmlOutput: File = new File(s"census.pmml")

    val os = new java.io.FileOutputStream(pmmlOutput.getAbsolutePath())  
    MetroJAXBUtil.marshalPMML(pmml, os)

    val baos = new java.io.ByteArrayOutputStream()  
    MetroJAXBUtil.marshalPMML(pmml, baos)

/*
    import org.apache.http.client.methods.HttpPost
    import org.apache.http.entity.StringEntity
    import org.apache.http.impl.client.DefaultHttpClient // TODO:  this is deprecated

    // create an HttpPost object
    println("--- HTTP POST UPDATED PMML ---")
    val post = new HttpPost(s"http://demo.pipeline.io:9040/update-pmml/${pmmlName}")

    // set the Content-type
    post.setHeader("Content-type", "application/xml")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(baos.toString()))

    // send the post request
    val response = (new DefaultHttpClient).execute(post)

    // print the response headers
    println("--- HTTP RESPONSE HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg)

    val is = new java.io.FileInputStream(pmmlOutput.getAbsolutePath())
    val transformedSource = ImportFilter.apply(new InputSource(is))

    val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)

    import org.jpmml.evaluator.Evaluator
    import org.jpmml.evaluator.FieldValue
    import org.jpmml.evaluator.ModelEvaluatorFactory
    import org.jpmml.evaluator.EvaluatorUtil

    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

    val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml2)
    System.out.println("Mining function: " + modelEvaluator.getMiningFunction())

    val inputFields = modelEvaluator.getInputFields().asScala

    System.out.println("Input schema:");
    System.out.println("\t" + "Input fields: " + inputFields) 

    System.out.println("Output schema:");
    System.out.println("\t" + "Target fields: " + modelEvaluator.getTargetFields())
    System.out.println("\t" + "Output fields: " + modelEvaluator.getOutputFields())

    val inputs: Map[String, _] = Map("age" -> 39, 
                                 "workclass" -> "State-gov",
                                 "education" -> "Bachelors",
                                 "education_num" -> 13,
                                 "marital_status" -> "Never-married",
                                 "occupation" -> "Adm-clerical",
                                 "relationship" -> "Not-in-family",
                                 "race" -> "White",
                                 "sex" -> "Male",
                                 "capital_gain" -> 2174,
                                 "capital_loss" -> 0,
                                 "hours_per_week" -> 40,
                                 "native_country" -> "United-States")

    val arguments = 
      ( for(inputField <- inputFields) 
        // The raw value is passed through: 
        //   1) outlier treatment, 
        //   2) missing value treatment, 
        //   3) invalid value treatment 
        //   4) type conversion
        yield (inputField.getName -> inputField.prepare(inputs(inputField.getName.getValue)))
      ).toMap.asJava

    val results = modelEvaluator.evaluate(arguments)
    val targetField = modelEvaluator.getTargetFields().asScala(0)
    val targetValue = results.get(targetField)

    System.out.println(s"**** Predicted value for '${targetField.getName}': ${targetValue} ****")
*/
  }
}
