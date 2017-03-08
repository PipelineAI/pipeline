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
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.apache.commons.io.IOUtils
import java.net.URL
import java.nio.charset.Charset
import java.io.File
import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.FieldValue
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.evaluator.EvaluatorUtil

object EvaluatePMML { 
  def evaluate(pmmlPath: String, inputJson: String) = {
    val is = new java.io.FileInputStream(pmmlPath)
    val transformedSource = ImportFilter.apply(new InputSource(is))

    val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)
    
    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

    val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml2)
    System.out.println("Mining function: " + modelEvaluator.getMiningFunction())

    val inputFields = modelEvaluator.getInputFields().asScala

    System.out.println("Input schema:");
    System.out.println("\t" + "Input fields: " + inputFields) 

    System.out.println("Output schema:");
    System.out.println("\t" + "Target fields: " + modelEvaluator.getTargetFields())
    System.out.println("\t" + "Output fields: " + modelEvaluator.getOutputFields())

    // TODO:  Convert inputJson into Map
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
    val targetValue = results.get(targetField.getName)

    System.out.println(s"**** Predicted value for '${targetField.getName}': ${targetValue} ****")
  }
}
