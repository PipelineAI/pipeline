package io.pipeline.prediction.tensorflow

import java.io.FileOutputStream

import scala.collection.immutable.HashMap
import scala.io.Source
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import javax.servlet.annotation.MultipartConfig
import java.io.InputStream
import scala.util.parsing.json.JSON

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("prediction_tensorflow")
  new StandardExports().register()

/*  
 curl -i -X POST -v -H "Transfer-Encoding: chunked" \
   -F "model=@tensorflow_inception_graph.pb" \
   http://[host]:[port]/api/v1/update-tensorflow/default/tensorflow_inception/1
*/
  @RequestMapping(path=Array("/api/v1/model/deploy/tensorflow/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def updateTensorflow(@PathVariable("namespace") namespace: String,
                       @PathVariable("modelName") modelName: String, 
                       @PathVariable("version") version: String,
                       @RequestParam("file") file: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      val filename = file.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"store/${namespace}/${modelName}/export/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      inputStream = file.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"store/${namespace}/${modelName}/export/${version}/${filename}"))
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
    } finally {
      if (inputStream != null) {
        inputStream.close()
      }
    }

    new ResponseEntity(HttpStatus.OK)
  }

  @RequestMapping(path=Array("/api/v1/model/predict/tensorflow-grpc/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictTensorFlowGrpc(@PathVariable("namespace") namespace: String,
                             @PathVariable("modelName") modelName: String, 
                             @PathVariable("version") version: Integer, 
                             @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val results = new TensorflowGrpcCommand(s"${modelName}_grpc", namespace, modelName, version, inputs, "fallback", 5000, 20, 10)
        .execute()
        
      s"""{"results":[${results}]"""
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
    }
  }
  
  @RequestMapping(path=Array("/api/v1/model/predict/tensorflow-java/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateTensorflowNative(@PathVariable("namespace") namespace: String,
                               @PathVariable("modelName") modelName: String, 
                               @PathVariable("version") version: Integer,
                               @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val results = new TensorflowNativeCommand(s"${modelName}_java", namespace, modelName, version, inputs, "fallback", 5000, 20, 10)
        .execute()

      s"""{"results":[${results}]"""
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
    }
  }

/*  
   curl -i -X POST -v -H "Transfer-Encoding: chunked" \
    -F "image=@1.jpg" \
    http://[host]:[port]/api/v1/model/predict/tensorflow-java-with-image/default/tensorflow_inception/1
*/
  @RequestMapping(path=Array("/api/v1/model/predict/tensorflow-java-with-image/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def preidctTensorFlowJavaWithImage(@PathVariable("namespace") namespace: String,
                                      @PathVariable("modelName") modelName: String,
                                      @PathVariable("version") version: Integer,
                                      @RequestParam("image") image: MultipartFile): String = {
    try {
      val inputs = new HashMap[String,Any]()
  
      // Get name of uploaded file.
      val filename = image.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"store/${namespace}/images/${modelName}/${version}/")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      val inputStream = image.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"store/${namespace}/images/${modelName}/${version}/${filename}"),
        StandardCopyOption.REPLACE_EXISTING)
  
      inputStream.close()
  
      val results = new TensorflowJavaWithImageCommand(s"${modelName}_image", namespace, modelName, version, filename, inputs, "fallback", 5000, 20, 10)
          .execute()
  
      s"""{"results":[${results}]"""
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
    }
  }
  
  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  -F "image=@1.jpg" \
  //  http://[host]:[port]/api/v1/evaluate-tensorflow-grpc-image/default/tensorflow_inception/1
  @RequestMapping(path=Array("/api/v1/model/predict/tensorflow-grpc-with-image/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def predictTensorFlowGrpcWithImage(@PathVariable("namespace") namespace: String,
                                      @PathVariable("modelName") modelName: String,
                                      @PathVariable("version") version: Integer,
                                      @RequestParam("image") image: MultipartFile): String = {
    try {
      val inputs = new HashMap[String,Any]()
  
      // Get name of uploaded file.
      val filename = image.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"store/${namespace}/images/${modelName}/${version}/")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      val inputStream = image.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"store/${namespace}/images/${modelName}/${version}/${filename}"),
        StandardCopyOption.REPLACE_EXISTING)
  
      inputStream.close()
  
      val results = new TensorflowGrpcWithImageCommand(s"${modelName}_image", namespace, modelName, version, filename, inputs, "fallback", 5000, 20, 10)
          .execute()
  
      s"""{"results":[${results}]"""
    } catch {
      case e: Throwable => {
        System.out.println(e)
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
