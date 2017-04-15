package com.advancedspark.serving.prediction.python

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.parsing.json.JSON

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.file.Files
import org.springframework.web.bind.annotation.RequestParam
import java.nio.file.Paths
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import java.nio.file.FileSystems
import scala.reflect.io.File


@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("prediction_python")
  new StandardExports().register()
  
  val responseHeaders = new HttpHeaders();

  @RequestMapping(path=Array("/update-python/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateSource(@PathVariable("namespace") namespace: String,
                   @PathVariable("modelName") modelName: String,
                   @PathVariable("version") version: String,                   
                   @RequestBody source: String): 
      ResponseEntity[String] = {
    Try {
      System.out.println(s"Updating source for ${namespace}/${modelName}/${version}:\n${source}")

      // Write the new java source to local disk
      val path = new java.io.File(s"store/${namespace}/${modelName}/${version}/")
      if (!path.isDirectory()) {
        path.mkdirs()
      }

      val file = new java.io.File(s"store/${namespace}/${modelName}/${version}/${modelName}.py")
      if (!file.exists()) {
        file.createNewFile()
      }

      val fos = new java.io.FileOutputStream(file)
      fos.write(source.getBytes())

      new ResponseEntity[String](source, responseHeaders, HttpStatus.OK)
    } match {
      case Failure(t: Throwable) => {
        val responseHeaders = new HttpHeaders();
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
          HttpStatus.BAD_REQUEST)
      }
      case Success(response) => response      
    }
  }
 
  /*  
   curl -i -X POST -v -H "Transfer-Encoding: chunked" \
     -F "model=@tensorflow_inception_graph.pb" \
     http://[host]:[port]/update-tensorflow/default/tensorflow_inception/1
  */
  @RequestMapping(path=Array("/update-python-bundle/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def updateTensorflow(@PathVariable("namespace") namespace: String,
                       @PathVariable("modelName") modelName: String, 
                       @PathVariable("version") version: String,
                       @RequestParam("bundle") bundle: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      // TODO:  only preserve last part of filename (not parent path)
      val filename = bundle.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"store/${namespace}/${modelName}/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'bundle' multipart file
      inputStream = bundle.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"store/${namespace}/${modelName}/${version}/${filename}"))
      
      val uploadedFilePath = Paths.get(s"store/${namespace}/${modelName}/${version}/${filename}")
      
      ZipFileUtil.unzip(uploadedFilePath.toFile.getAbsolutePath, uploadedFilePath.getParent.toFile.getAbsolutePath)
            
      // TODO:  Improve this
      val p0 = Runtime.getRuntime().exec(s"PIO_MODEL_NAMESPACE=${namespace} PIO_MODEL_NAME=${modelName} PIO_MODEL_VERSION=${version} delete_environment")
      val p1 = Runtime.getRuntime().exec(s"PIO_MODEL_NAMESPACE=${namespace} PIO_MODEL_NAME=${modelName} PIO_MODEL_VERSION=${version} create_environment")
      System.out.println("p1: " + p1)      
      
      val stdInput1 = new BufferedReader(new InputStreamReader(p1.getInputStream()));
      System.out.println("stdInput: " + stdInput1)

      val stdError1 = new BufferedReader(new InputStreamReader(p1.getErrorStream()));
      System.out.println("stdError: " + stdError1)

      // read the output from the command
      val success = stdInput1.lines().collect(Collectors.joining("\n"))
      //System.out.println("success: " + success)

      val error = stdError1.lines().collect(Collectors.joining("\n"))
      //System.out.println("error: " + error)

      val port = 9876
      val parentPath = uploadedFilePath.getParent
                  
      // Find .pkl file
      val modelPklFilename = parentPath.toFile.listFiles.filter(_.getAbsolutePath.endsWith(".pkl")).map(_.getName)
      System.out.println("modelPklFilename: " + modelPklFilename)
      
      val p2 = Runtime.getRuntime().exec(s"PIO_MODEL_NAMESPACE=${namespace} PIO_MODEL_NAME=${modelName} PIO_MODEL_VERSION=${version} PIO_MODEL_FILENAME=${modelPklFilename} PIO_MODEL_SERVER_PORT=${port} spawn_model_server")
      System.out.println("p2: " + p2)    
      
      val stdInput2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
      System.out.println("stdInput2: " + stdInput2)

      val stdError2 = new BufferedReader(new InputStreamReader(p2.getErrorStream()));
      System.out.println("stdError2: " + stdError2)

      // read the output from the command
      val success2 = stdInput2.lines().collect(Collectors.joining("\n"))
      //System.out.println("success: " + success)

      val error2 = stdError2.lines().collect(Collectors.joining("\n"))
      //System.out.println("error: " + error)
      
      var result = s"""{"result":"${success + success2}""""
      
      if (error.length() > 0) {
        System.out.println("error: " + error + error2)
        result = result + s""", "error":"${error + error2}""""
      }
      
      result + "}"          
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
  
  @RequestMapping(path=Array("/evaluate-python/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateSource(@PathVariable("namespace") namespace: String,
                     @PathVariable("modelName") modelName: String, 
                     @PathVariable("version") version: String,
                     @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
      val inputs = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]

      val filename = s"${modelName}.py"

      val result = new PythonSourceCodeEvaluationCommand(modelName, namespace, version, filename, inputJson, "fallback", 10000, 20, 10).execute()

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
