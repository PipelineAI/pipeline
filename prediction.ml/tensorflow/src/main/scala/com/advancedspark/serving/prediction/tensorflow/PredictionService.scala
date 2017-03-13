package com.advancedspark.serving.prediction.tensorflow

import java.io.FileOutputStream

import scala.collection.immutable.HashMap
import scala.io.Source
import java.nio.file.Files
import java.nio.file.Paths
//import java.nio.file.StandardCopyOption

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
//import org.springframework.cloud.netflix.zuul.EnableZuulProxy
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

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
//@EnableZuulProxy
class PredictionService {
  val modelRegistry = new scala.collection.mutable.HashMap[String, Array[Byte]]

  HystrixPrometheusMetricsPublisher.register("prediction_tensorflow")
  new StandardExports().register()

  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  -F "model=@tensorflow_inception_graph.pb" \
  //  http://prediction-tensorflow-aws.demo.pipeline.io/update-tensorflow-model/tensorflow_inception/00000001
  @RequestMapping(path=Array("/update-tensorflow-model/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def updateTensorflow(@PathVariable("modelName") modelName: String, 
                       @PathVariable("version") version: String,
                       @RequestParam("model") model: MultipartFile): ResponseEntity[HttpStatus] = {

    // Get name of uploaded file.
    val filename = model.getOriginalFilename()

    // Path where the uploaded file will be stored.
    val filepath = new java.io.File(s"store/${modelName}/export/${version}")
    if (!filepath.isDirectory()) {
      filepath.mkdirs()
    }

    // This buffer will store the data read from 'model' multipart file
    val inputStream = model.getInputStream()

    Files.copy(inputStream, Paths.get(s"store/${modelName}/export/${version}/${filename}"))
     // StandardCopyOption.REPLACE_EXISTING)

    inputStream.close()

    return new ResponseEntity(HttpStatus.OK)
  }

  @RequestMapping(path=Array("/evaluate-tensorflow-grpc/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateTensorflowGrpc(@PathVariable("modelName") modelName: String, 
                             @PathVariable("version") version: String, 
                             @RequestBody inputJson: String): String = {
    try {
      val inputs = new HashMap[String,Any]()
        //JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val results = new TensorflowGrpcCommand("tensorflow_grpc_${modelName}", modelName, version, inputs, "fallback", 2000, 20, 10)
        .execute()

      s"""{"results":[${results}]"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  @RequestMapping(path=Array("/evaluate-tensorflow-java/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateTensorflowNative(@PathVariable("modelName") modelName: String, 
                               @PathVariable("version") version: String,
                               @RequestBody inputJson: String): String = {
    try {
      val inputs = new HashMap[String,Any]()
        //JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val results = new TensorflowNativeCommand(s"tensorflow_java_${modelName}", modelName, version, inputs, "fallback", 2000, 20, 10)
        .execute()

      s"""{"results":[${results}]"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  -F "image=@1.jpg" \
  //  http://prediction-tensorflow-aws.demo.pipeline.io/evaluate-tensorflow-with-image/tensorflow_inception/00000001
  @RequestMapping(path=Array("/evaluate-tensorflow-with-image/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def evaluateTensorflowWithImage(@PathVariable("modelName") modelName: String,
                                  @PathVariable("version") version: String,
                                  @RequestParam("image") image: MultipartFile): String = {

    val inputs = new HashMap[String,Any]()
    //JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

    // Get name of uploaded file.
    val filename = image.getOriginalFilename()

    // Path where the uploaded file will be stored.
    val filepath = new java.io.File(s"images/")
    if (!filepath.isDirectory()) {
      filepath.mkdirs()
    }

    // This buffer will store the data read from 'model' multipart file
    val inputStream = image.getInputStream()

    Files.copy(inputStream, Paths.get(s"images/${filename}"))
    // StandardCopyOption.REPLACE_EXISTING)

    inputStream.close()

    val results = new TensorflowNativeWithImageCommand(s"tensorflow_java_image_${modelName}", modelName, version, filename, inputs, "fallback", 2000, 20, 10)
        .execute()

    s"""{"results":[${results}]"""
  }  
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
