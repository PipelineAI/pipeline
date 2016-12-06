package com.advancedspark.serving.prediction.codegen

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
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation._
import scala.util.{Try,Success,Failure}

@SpringBootApplication
@RestController
@EnableHystrix
class PredictionService {
  val predictorRegistry = new scala.collection.mutable.HashMap[String, Predictable]
  val responseHeaders = new HttpHeaders();

  @RequestMapping(path=Array("/update-source/{className}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateSource(@PathVariable("className") className: String, @RequestBody classSource: String): 
      ResponseEntity[String] = {
    Try {
      System.out.println(s"Generating source for ${className}: ${classSource}")

      val (predictor, generatedCode) = PredictorCodeGenerator.codegen(className, classSource)
      
      System.out.println(s"Updating codegen cache for ${className}")
      
      // Update Predictor in Cache
      predictorRegistry.put(className, predictor)

      new ResponseEntity[String](generatedCode, responseHeaders, HttpStatus.OK)
    } match {
      case Failure(t: Throwable) => {
        val responseHeaders = new HttpHeaders();
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
          HttpStatus.BAD_REQUEST)
      }
      case Success(response) => response      
    }
  }
 
  @RequestMapping(path=Array("/evaluate-source/{className}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateSource(@PathVariable("className") className: String, @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
      val predictorOption = predictorRegistry.get(className)

      val inputs = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val result = predictorOption match {
        case None => throw new Exception(s"No Source Found for ${className}")
        case Some(predictor) => new SourceCodeEvaluationCommand(className, predictor, inputs)
          .execute()
      } 

      new ResponseEntity[String](s"${result}", responseHeaders,
           HttpStatus.OK)
    } match {
      case Failure(t: Throwable) => {
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
          HttpStatus.BAD_REQUEST)
      }
      case Success(response) => response
    }   
  }
}

object PredictorCodeGenerator {
  def codegen(className: String, classSource: String): (Predictable, String) = {   
    val references = Map[String, Any]()

    val codeGenBundle = new CodeGenBundle(className,
        null, 
        Array(classOf[Initializable], classOf[Predictable], classOf[Serializable]), 
        Array(classOf[java.util.HashMap[String, Any]], classOf[java.util.Map[String, Any]]), 
        CodeFormatter.stripExtraNewLines(classSource)
    )
    
    Try {
      val clazz = CodeGenerator.compile(codeGenBundle)
      val generatedCode = CodeFormatter.format(codeGenBundle)

      System.out.println(s"\n${generatedCode}}")      
            
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references)

      (bar.asInstanceOf[Predictable], generatedCode)
    } match {
      case Failure(t) => {
        System.out.println(s"Could not generate code: ${codeGenBundle}", t)
        throw t
      }
      case Success(tuple) => tuple
    }
  }
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
