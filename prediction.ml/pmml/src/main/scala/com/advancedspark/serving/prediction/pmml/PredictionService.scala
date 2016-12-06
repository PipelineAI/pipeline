package com.advancedspark.serving.prediction.pmml

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.parsing.json.JSON

import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil
import org.xml.sax.InputSource

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.context.config.annotation._
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.context.annotation._
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._

import org.jpmml.evaluator.visitors.PredicateOptimizer
import org.jpmml.evaluator.visitors.PredicateInterner

@SpringBootApplication
@RestController
@EnableHystrix
class PredictionService {
  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]

  @RequestMapping(path=Array("/update-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody pmmlString: String): 
      ResponseEntity[HttpStatus] = {
    try {
      // Write the new pmml (XML format) to local disk
      val path = new java.io.File(s"data/${pmmlName}/")
      if (!path.isDirectory()) { 
        path.mkdirs()
      }

      val file = new java.io.File(s"data/${pmmlName}/${pmmlName}.pmml")
      if (!file.exists()) {
        file.createNewFile()
      }

      val os = new java.io.FileOutputStream(file)
      os.write(pmmlString.getBytes())    

      val transformedSource = ImportFilter.apply(new InputSource(new java.io.StringReader(pmmlString)))

      val pmml = JAXBUtil.unmarshalPMML(transformedSource)

      val predicateOptimizer = new PredicateOptimizer()
      predicateOptimizer.applyTo(pmml)

      val predicateInterner = new PredicateInterner()
      predicateInterner.applyTo(pmml)

      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml)

      // Update PMML in Cache
      pmmlRegistry.put(pmmlName, modelEvaluator)
      
      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/evaluate-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody inputJson: String): String = {

    try {
      var modelEvaluator: Evaluator = null 
      val modelEvaluatorOption = pmmlRegistry.get(pmmlName)
      if (modelEvaluatorOption == None) {
        val is = new java.io.FileInputStream(s"data/${pmmlName}/${pmmlName}.pmml")
        val transformedSource = ImportFilter.apply(new InputSource(is))

        val pmml = JAXBUtil.unmarshalPMML(transformedSource)

        val predicateOptimizer = new PredicateOptimizer()
        predicateOptimizer.applyTo(pmml)

        val predicateInterner = new PredicateInterner()
        predicateInterner.applyTo(pmml)

        val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

        modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)

        // Cache modelEvaluator
        pmmlRegistry.put(pmmlName, modelEvaluator)
      } else {
        modelEvaluator = modelEvaluatorOption.get
      }

      val inputs = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val results = new PMMLEvaluationCommand(pmmlName, modelEvaluator, inputs)
       .execute()

      s"""{"results":[${results}]"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
