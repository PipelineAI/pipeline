package com.advancedspark.pmml.spark.ml

import java.io.File

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
import org.jpmml.sparkml.ConverterUtil
import org.jpmml.model.MetroJAXBUtil
import org.jpmml.model.ImportFilter
import org.xml.sax.InputSource
import org.jpmml.model.JAXBUtil
import org.jpmml.evaluator.TreeModelEvaluator
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.evaluator.Evaluator

object PMMLSparkML {
  val csvInput: File = new File("census.csv")

  val functionType: String = "classification" // or "regression"

  val formulaStr: String = "income ~ ."

  val pmmlOutput: File = new File("census.pmml")

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

    //////////////////////////////////////////////////////////////////////////////// 
    // TODO:  Uncomment this once we move to Spark 2.0.0 (or shade under Spark 1.6.1)
//    val pmml = ConverterUtil.toPMML(schema, pipelineModel)
//
//    val os = new java.io.FileOutputStream(pmmlOutput.getAbsolutePath())  
//    MetroJAXBUtil.marshalPMML(pmml, os)
//    
//    // Form the following:  https://github.com/jpmml/jpmml-evaluator
//    val is = new java.io.FileInputStream(pmmlOutput.getAbsolutePath())
//    val transformedSource = ImportFilter.apply(new InputSource(is))
//
//    val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)
//    
//    //val modelEvaluator = new TreeModelEvaluator(pmml2)    
//    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
//
//    val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelManager(pmml2)
  }
}

