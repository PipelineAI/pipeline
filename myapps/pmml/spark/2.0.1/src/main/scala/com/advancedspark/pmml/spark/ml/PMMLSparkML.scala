package com.advancedspark.pmml.spark.ml

import java.io.File

import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.PipelineStage
import org.apache.spark.ml.Predictor
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.feature.RFormula
import org.apache.spark.ml.regression.DecisionTreeRegressor
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.DataFrameReader
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructType
import org.dmg.pmml.FieldName
import org.dmg.pmml.DataField
import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.FieldValue
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.evaluator.EvaluatorUtil
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.jpmml.model.MetroJAXBUtil
import org.jpmml.sparkml.ConverterUtil
import org.xml.sax.InputSource
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient // TODO:  this is deprecated

object PMMLSparkML {
  val datasetsHome = sys.env.get("DATASETS_HOME").getOrElse("/root/pipeline/datasets/")

  val csvInput: File = new File(s"${datasetsHome}/R/census.csv")

  val functionType: String = "classification" // or "regression"

  val formulaStr: String = "income ~ ."

  val pmmlName = "census"
  val pmmlOutput: File = new File(s"${pmmlName}.pmml")

  def main(args: Array[String]) = {
    val sparkConf: SparkConf = new SparkConf()

    val sparkContext: JavaSparkContext = new JavaSparkContext(sparkConf)
    
    val sqlContext: SQLContext = new SQLContext(sparkContext)

    val reader: DataFrameReader = sqlContext.read
        .format("com.databricks.spark.csv")
        .option("header", "true")
        .option("inferSchema", "true")
        
    val dataFrame: DataFrame = reader.load(csvInput.getAbsolutePath())

    val schema: StructType = dataFrame.schema
    System.out.println(schema.treeString)

    val formula: RFormula = new RFormula().setFormula(formulaStr)

    var predictor: Predictor[_, _, _] = null 

    if (functionType.equals("classification")) {
      predictor = new DecisionTreeClassifier().setMinInstancesPerNode(10)
    } else  
    if (functionType.equals("regression")) {
      predictor = new DecisionTreeRegressor().setMinInstancesPerNode(10)
    }
    else 
      throw new IllegalArgumentException()
  
    predictor.setLabelCol(formula.getLabelCol)
    predictor.setFeaturesCol(formula.getFeaturesCol)

    val pipeline = new Pipeline().setStages(Array[PipelineStage](formula, predictor))

    val pipelineModel = pipeline.fit(dataFrame)
    
    val predictorModel = pipeline.getStages(1).asInstanceOf[DecisionTreeClassifier]
    System.out.println(predictorModel.explainParams())

    // Note:  This requires latest version of org.jpmml:jpmml-sparkml which requires shading
    //        to avoid conflict with Spark 1.6.1
    val pmml = ConverterUtil.toPMML(schema, pipelineModel)
    System.out.println(pmml.getModels().get(0).toString())

    val os = new java.io.FileOutputStream(pmmlOutput.getAbsolutePath())  
    MetroJAXBUtil.marshalPMML(pmml, os)
    
    val baos = new java.io.ByteArrayOutputStream()  
    MetroJAXBUtil.marshalPMML(pmml, baos)

    // create an HttpPost object
    println("--- HTTP POST UPDATED PMML ---")
    val post = new HttpPost(s"http://127.0.0.1:9040/update-pmml/${pmmlName}")

    // set the Content-type
    post.setHeader("Content-type", "application/xml")

    // add the JSON as a StringEntity
    post.setEntity(new StringEntity(baos.toString()))

    // send the post request
    val response = (new DefaultHttpClient).execute(post)

    // print the response headers
    println("--- HTTP RESPONSE HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))

    // Form the following:  https://github.com/jpmml/jpmml-evaluator
    val is = new java.io.FileInputStream(pmmlOutput.getAbsolutePath())
    val transformedSource = ImportFilter.apply(new InputSource(is))

    val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)
    
    //val modelEvaluator = new TreeModelEvaluator(pmml2)    
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
    val targetValue = results.get(targetField.getName)
    
    System.out.println(s"**** Predicted value for '${targetField.getName}': ${targetValue} ****")
  }
}
