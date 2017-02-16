package com.advancedspark.serving.prediction.python

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
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import java.util.stream.Collectors
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("prediction_python")
  
  val responseHeaders = new HttpHeaders();

  @RequestMapping(path=Array("/update-python/{name}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateSource(@PathVariable("name") name: String, @RequestBody source: String): 
      ResponseEntity[String] = {
    Try {
//      val predictorRegistry = new scala.collection.mutable.HashMap[String, Predictable]

//      System.out.println(s"Generating source for ${name}:\n${source}")

      // Write the new java source to local disk
      val path = new java.io.File(s"data/${name}/")
      if (!path.isDirectory()) {
        path.mkdirs()
      }

      val file = new java.io.File(s"data/${name}/${name}.py")
      if (!file.exists()) {
        file.createNewFile()
      }

      val fos = new java.io.FileOutputStream(file)
      fos.write(source.getBytes())

//      val (predictor, generatedCode) = PredictorCodeGenerator.codegen(name, source)
      
//      System.out.println(s"Updating cache for ${name}:\n${generatedCode}")
      
      // Update Predictor in Cache
//      predictorRegistry.put(name, predictor)

      new ResponseEntity[String](responseHeaders, HttpStatus.OK)
    } match {
      case Failure(t: Throwable) => {
        val responseHeaders = new HttpHeaders();
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
          HttpStatus.BAD_REQUEST)
      }
      case Success(response) => response      
    }
  }
 
  @RequestMapping(path=Array("/evaluate-python/{name}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateSource(@PathVariable("name") name: String, @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
//      val predictorOption = predictorRegistry.get(name)

      val inputs = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val filename = s"data/${name}/${name}.py"

		  //read file into stream
      //val source = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"))
			    
      val result = new PythonSourceCodeEvaluationCommand(name, filename, inputs, "fallback", 1000, 20, 10).execute()

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

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
