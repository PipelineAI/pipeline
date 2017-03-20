package com.advancedspark.serving.prediction.pmml

import scala.util.parsing.json.JSON

import org.jpmml.evaluator.Evaluator
import org.jpmml.evaluator.ModelEvaluatorFactory
import org.jpmml.evaluator.visitors.PredicateInterner
import org.jpmml.evaluator.visitors.PredicateOptimizer
import org.jpmml.model.ImportFilter
import org.jpmml.model.JAXBUtil

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import org.xml.sax.InputSource

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import io.prometheus.client.hotspot.StandardExports

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]

  HystrixPrometheusMetricsPublisher.register("prediction_pmml")
  
  new StandardExports().register()
    
  @RequestMapping(path=Array("/update-pmml/{namespace}/{pmmlName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/xml; charset=UTF-8"))
  def updatePmml(@PathVariable("namespace") namespace: String, 
                 @PathVariable("pmmlName") pmmlName: String, 
                 @PathVariable("version") version: String,
                 @RequestBody pmmlString: String): 
      ResponseEntity[HttpStatus] = {
    try {
      // Write the new pmml (XML format) to local disk
      val path = new java.io.File(s"store/${namespace}/${pmmlName}/${version}")
      if (!path.isDirectory()) { 
        path.mkdirs()
      }

      val file = new java.io.File(s"store/${namespace}/${pmmlName}/${version}/${pmmlName}.pmml")
      if (!file.exists()) {
        file.createNewFile()
      }

      val fos = new java.io.FileOutputStream(file)
      fos.write(pmmlString.getBytes())    

      val transformedSource = ImportFilter.apply(new InputSource(new java.io.StringReader(pmmlString)))

      val pmml = JAXBUtil.unmarshalPMML(transformedSource)

      val predicateOptimizer = new PredicateOptimizer()
      predicateOptimizer.applyTo(pmml)

      val predicateInterner = new PredicateInterner()
      predicateInterner.applyTo(pmml)

      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml)

      // Update PMML in Cache
      pmmlRegistry.put(namespace + "/" + pmmlName + "/" + version, modelEvaluator)
      
      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/evaluate-pmml/{namespace}/{pmmlName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluatePmml(@PathVariable("namespace") namespace: String, 
                   @PathVariable("pmmlName") pmmlName: String, 
                   @PathVariable("version") version: String,
                   @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val modelEvaluatorOption = pmmlRegistry.get(namespace + "/" + pmmlName + "/" + version)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          val fis = new java.io.FileInputStream(s"store/${namespace}/${pmmlName}/${version}/${pmmlName}.pmml")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(namespace + "/" + pmmlName + "/" + version, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand(pmmlName, namespace, pmmlName, version, modelEvaluator, inputs, s"""{"result": "fallback"}""", 25, 20, 10)
       .execute()

      s"""{"results":[${results}]}"""
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