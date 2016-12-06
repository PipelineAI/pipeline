package com.advancedspark.serving.prediction

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.parsing.json.JSON

import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.xml.sax.InputSource

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import java.io.StringReader

class PMMLEvaluationCommand(modelEvaluator: Evaluator, inputJson: String) 
    extends HystrixCommand[String](HystrixCommandGroupKey.Factory.asKey("PMMLEvaluationCommand")) {

  def run(): String = {
    try{
      val inputsMap = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val inputFields = modelEvaluator.getInputFields().asScala

      val arguments =
        ( for(inputField <- inputFields)
          // The raw value is passed through:
          //   1) outlier treatment,
          //   2) missing value treatment,
          //   3) invalid value treatment
          //   4) type conversion
          yield (inputField.getName -> inputField.prepare(inputsMap(inputField.getName.getValue)))
        ).toMap.asJava

      val results = modelEvaluator.evaluate(arguments)
      val targetField = modelEvaluator.getTargetFields().asScala(0)
      val targetValue = results.get(targetField.getName)

      s"""[{'${targetField.getName}': '${targetValue}'}]"""
    } catch { 
       case e: Throwable => {
         // System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): String = {
    // System.out.println("PMML Evaluator is Down!  Fallback!!")

    s"""[]"""
  }
}
