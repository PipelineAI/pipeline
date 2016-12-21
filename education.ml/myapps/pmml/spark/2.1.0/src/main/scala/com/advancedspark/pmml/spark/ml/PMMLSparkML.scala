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
import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.FieldValue
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.jpmml.model.MetroJAXBUtil
import org.jpmml.sparkml.ConverterUtil
import org.xml.sax.InputSource

object PMMLSparkML {
  val datasetsHome = sys.env.get("DATASETS_HOME").getOrElse("/root/pipeline/datasets/")

  val csvInput: File = new File(s"${datasetsHome}/R/census.csv")

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

    // Note:  This requires latest version of org.jpmml:jpmml-sparkml which requires shading
    //        to avoid conflict with Spark 1.6.1
    val pmml = ConverterUtil.toPMML(schema, pipelineModel)

    val os = new java.io.FileOutputStream(pmmlOutput.getAbsolutePath())  
    MetroJAXBUtil.marshalPMML(pmml, os)
    
    // Form the following:  https://github.com/jpmml/jpmml-evaluator
    val is = new java.io.FileInputStream(pmmlOutput.getAbsolutePath())
    val transformedSource = ImportFilter.apply(new InputSource(is))

    val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)
    
    //val modelEvaluator = new TreeModelEvaluator(pmml2)    
    val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

    val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelManager(pmml2)
    System.out.println("Mining function: " + modelEvaluator.getMiningFunction())

    val activeFields = modelEvaluator.getActiveFields().asScala

    System.out.println("Input schema:");
    System.out.println("\t" + "Active fields: " + modelEvaluator.getActiveFields())
    System.out.println("\t" + "Group fields: " + modelEvaluator.getGroupFields())

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
      ( for(activeField <- activeFields) 
        // The raw value is passed through: 
        //   1) outlier treatment, 
        //   2) missing value treatment, 
        //   3) invalid value treatment 
        //   4) type conversion
        yield (activeField -> modelEvaluator.prepare(activeField, inputs(activeField.getValue)))
      ).toMap.asJava

    val results = modelEvaluator.evaluate(arguments)
    val targetName = modelEvaluator.getTargetField()
    val targetValue = results.get(targetName)
    
    System.out.println(s"**** Predicted value for '${targetName.getValue}': ${targetValue} ****")

    //if (targetValue instanceof Computable) {
    //    Computable computable = (Computable)targetValue;

    //    Object primitiveValue = computable.getResult();
    //}
  }
}
