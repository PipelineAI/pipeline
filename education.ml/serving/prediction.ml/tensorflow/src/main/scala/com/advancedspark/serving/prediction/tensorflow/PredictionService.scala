package com.advancedspark.serving.prediction.tensorflow

import scala.collection.immutable.HashMap

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import org.springframework.web.bind.annotation.RequestMethod
import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  val modelRegistry = new scala.collection.mutable.HashMap[String, Array[Byte]]

  HystrixPrometheusMetricsPublisher.register("prediction_tensorflow")
  new StandardExports().register()

  @RequestMapping(path=Array("/update-tensorflow/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateModel(@PathVariable("modelName") modelName: String, @RequestBody model: Array[Byte]):
      ResponseEntity[HttpStatus] = {

    try {
      // Write the new newModel to local disk
      val path = new java.io.File(s"store/${modelName}/")
      if (!path.isDirectory()) {
        path.mkdirs()
      }

      val file = new java.io.File(s"store/${modelName}/${modelName}.pb")
      if (!file.exists()) {
        file.createNewFile()
      }

      val os = new java.io.FileOutputStream(file)
      os.write(model)

      modelRegistry.put(modelName, model)

      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/evaluate-tensorflow/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateModel(@PathVariable("modelName") modelName: String, @RequestBody inputJson: String):
      String = {
    try {
      val inputs = new HashMap[String,Any]()
        //JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val results = new TensorflowGrpcCommand("127.0.0.1", 9000, modelName, inputs, "fallback", 25, 20, 10)
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
