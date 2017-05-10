package io.pipeline.prediction.jvm

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

import scala.collection.JavaConversions.mapAsJavaMap
import scala.util.Failure
import scala.util.Success
import scala.util.Try
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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.xml.sax.InputSource

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import org.springframework.web.bind.annotation.RequestMethod
import java.nio.file.StandardCopyOption

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("prediction_jvm")
  new StandardExports().register()

  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]
  val predictorRegistry = new scala.collection.mutable.HashMap[String, Predictable]
  val modelRegistry = new scala.collection.mutable.HashMap[String, Array[Byte]]
  
  val redisHostname = "redis-master"
  val redisPort = 6379
 
  val jedisPool = new JedisPool(new JedisPoolConfig(), redisHostname, redisPort);

  val responseHeaders = new HttpHeaders();

//  @RequestMapping(path=Array("/api/v1/model/deploy/java/{namespace}/{modelName}/{version}"),
//                  method=Array(RequestMethod.POST)
//                  //produces=Array("application/json; charset=UTF-8")
//                  )
//  def deployJavaString(@PathVariable("namespace") namespace: String, 
//                       @PathVariable("modelName") modelName: String,
//                       @PathVariable("version") version: String,
//                       @RequestBody source: String): 
//      ResponseEntity[String] = {
//    Try {
//      System.out.println(s"Generating source for 'java/${namespace}/${modelName}/${version}':\n${source}")
//
//      // Write the new java source to local disk
//      val path = new java.io.File(s"model_store/java/${namespace}/${modelName}/${version}")
//      if (!path.isDirectory()) {
//        path.mkdirs()
//      }
//
//      val file = new java.io.File(s"model_store/java/${namespace}/${modelName}/${version}/${modelName}.java")
//      if (!file.exists()) {
//        file.createNewFile()
//      }
//
//      val fos = new java.io.FileOutputStream(file)
//      fos.write(source.getBytes())
//
//      val (predictor, generatedCode) = PredictorCodeGenerator.codegen(modelName, source)
//      
//      System.out.println(s"Updating cache for 'java/${namespace}/${modelName}/${version}':\n${generatedCode}")
//      
//      // Update Predictor in Cache
//      predictorRegistry.put("java/" + namespace + "/" + modelName + "/" + version, predictor)
//
//      new ResponseEntity[String](generatedCode, responseHeaders, HttpStatus.OK)
//    } match {
//      case Failure(t: Throwable) => {
//        val responseHeaders = new HttpHeaders();
//        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
//          HttpStatus.BAD_REQUEST)
//      }
//      case Success(response) => response      
//    }
//  }
  
  @RequestMapping(path=Array("/api/v1/model/deploy/java/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST)
                  //produces=Array("application/json; charset=UTF-8")
                  )
  def deployJavaFile(@PathVariable("namespace") namespace: String, 
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
  
/*
    curl -i -X POST -v -H "Content-Type: application/json" \
      -d {"id":"21618"} \
      http://[hostname]:[port]/api/v1/model/predict/java/default/java_equals/1
*/
  @RequestMapping(path=Array("/api/v1/model/predict/java/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictJava(@PathVariable("namespace") namespace: String, 
                     @PathVariable("modelName") modelName: String, 
                     @PathVariable("version") version: String,
                     @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
      val predictorOption = predictorRegistry.get("java/" + namespace + "/" + modelName + "/" + version)

      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val predictor = predictorOption match {
        case None => {
          val sourceFileName = s"model_store/java/${namespace}/${modelName}/${version}/${modelName}.java"

          //read file into stream
          val stream: Stream[String] = Files.lines(Paths.get(sourceFileName))
			    
          // reconstuct original
          val source = stream.collect(Collectors.joining("\n"))
          
          val (predictor, generatedCode) = JavaCodeGenerator.codegen(modelName, source)

          System.out.println(s"Updating cache for 'java/${namespace}/${modelName}/${version}':\n${generatedCode}")
      
          // Update Predictor in Cache
          predictorRegistry.put("java/" + namespace + "/" + modelName + "/" + version, predictor)
      
          System.out.println(s"Updating cache for 'java/${namespace}/${modelName}/${version}':\n${generatedCode}")

          predictor
        }
        case Some(predictor) => {
           predictor
        }
      } 
          
      val result = new JavaSourceCodeEvaluationCommand(modelName, namespace, modelName, version, predictor, inputs, "fallback", 25, 20, 10).execute()

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

//  @RequestMapping(path=Array("/api/v1/model/deploy/pmml/{namespace}/{modelName}/{version}"),
//                  method=Array(RequestMethod.POST),
//                  produces=Array("application/xml; charset=UTF-8"))
//  def deployPmmlString(@PathVariable("namespace") namespace: String, 
//                       @PathVariable("modelName") modelName: String, 
//                       @PathVariable("version") version: String,
//                       @RequestBody pmmlString: String): 
//      ResponseEntity[HttpStatus] = {
//    try {
//      // Write the new pmml (XML format) to local disk
//      val path = new java.io.File(s"model_store/pmml/${namespace}/${modelName}/${version}")
//      if (!path.isDirectory()) { 
//        path.mkdirs()
//      }
//
//      val file = new java.io.File(s"model_store/pmml/${namespace}/${modelName}/${version}/${modelName}.pmml")
//      if (!file.exists()) {
//        file.createNewFile()
//      }
//
//      val fos = new java.io.FileOutputStream(file)
//      fos.write(pmmlString.getBytes())    
//
//      val transformedSource = ImportFilter.apply(new InputSource(new java.io.StringReader(pmmlString)))
//
//      val pmml = JAXBUtil.unmarshalPMML(transformedSource)
//
//      val predicateOptimizer = new PredicateOptimizer()
//      predicateOptimizer.applyTo(pmml)
//
//      val predicateInterner = new PredicateInterner()
//      predicateInterner.applyTo(pmml)
//
//      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
//
//      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
//
//      // Update PMML in Cache
//      pmmlRegistry.put("pmml/" + namespace + "/" + modelName + "/" + version, modelEvaluator)
//      
//      new ResponseEntity(HttpStatus.OK)
//    } catch {
//       case e: Throwable => {
//         throw e
//       }
//    }
//  }
  
  @RequestMapping(path=Array("/api/v1/model/deploy/pmml/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST)
                  //produces=Array("application/json; charset=UTF-8")
                  )
  def deployPmmlFile(@PathVariable("namespace") namespace: String, 
                     @PathVariable("modelName") modelName: String,
                     @PathVariable("version") version: String,
                     @RequestParam("file") file: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      val filename = file.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"model_store/pmml/${namespace}/${modelName}/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      inputStream = file.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"model_store/pmml/${namespace}/${modelName}/${version}/${modelName}.pmml"), StandardCopyOption.REPLACE_EXISTING)
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
 
  @RequestMapping(path=Array("/api/v1/model/predict/pmml/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictPmml(@PathVariable("namespace") namespace: String, 
                  @PathVariable("modelName") modelName: String, 
                  @PathVariable("version") version: String,
                  @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val modelEvaluatorOption = pmmlRegistry.get("pmml/" + namespace + "/" + modelName + "/" + version)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          val fis = new java.io.FileInputStream(s"model_store/pmml/${namespace}/${modelName}/${version}/${modelName}.pmml")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put("pmml/" + namespace + "/" + modelName + "/" + version, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand(modelName, namespace, modelName, version, modelEvaluator, inputs, s"""{"result": "fallback"}""", 25, 20, 10)
       .execute()

      s"""{"results":[${results}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }  
  
  @RequestMapping(path=Array("/api/v1/model/deploy/xgboost/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST)
                  //produces=Array("application/json; charset=UTF-8")
                  )
  def deployXgboostFile(@PathVariable("namespace") namespace: String, 
                        @PathVariable("modelName") modelName: String,
                        @PathVariable("version") version: String,
                        @RequestParam("file") file: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      val filename = file.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"model_store/xgboost/${namespace}/${modelName}/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      inputStream = file.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"model_store/xgboost/${namespace}/${modelName}/${version}/${modelName}.pmml"), StandardCopyOption.REPLACE_EXISTING)
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
 
  @RequestMapping(path=Array("/api/v1/model/predict/xgboost/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictXgboost(@PathVariable("namespace") namespace: String, 
                  @PathVariable("modelName") modelName: String, 
                  @PathVariable("version") version: String,
                  @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val modelEvaluatorOption = pmmlRegistry.get("xgboost/" + namespace + "/" + modelName + "/" + version)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          val fis = new java.io.FileInputStream(s"model_store/xgboost/${namespace}/${modelName}/${version}/${modelName}.pmml")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put("xgboost/" + namespace + "/" + modelName + "/" + version, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand(modelName, namespace, modelName, version, modelEvaluator, inputs, s"""{"result": "fallback"}""", 25, 20, 10)
       .execute()

      s"""{"results":[${results}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }    
  
  @RequestMapping(path=Array("/api/v1/model/predict/keyvalue/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def predictKeyValue(@PathVariable("namespace") namespace: String,
                      @PathVariable("collection") collection: String,
                      @PathVariable("version") version: String,
                      @PathVariable("userId") userId: String, 
                      @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemPredictionCommand("keyvalue_useritem", namespace, version, 25, 5, 10, -1.0d, userId, itemId)           
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  // Note that "keyvalue-batch" is a one-off.  Fix this.
  @RequestMapping(path=Array("/api/v1/model/predict/keyvalue-batch/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchPredictKeyValue(@PathVariable("namespace") namespace: String,
                           @PathVariable("collection") collection: String,
                           @PathVariable("version") version: String,
                           @PathVariable("userId") userId: String,
                           @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemBatchPredictionCollapser("keyvalue_useritem_batch", namespace, version, 25, 5, 10, -1.0d, userId, itemId)
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  @RequestMapping(path=Array("/api/v1/model/predict/keyvalue/{namespace}/{collection}/{version}/{userId}/{startIdx}/{endIdx}"), 
                  produces=Array("application/json; charset=UTF-8"))
  def recommendations(@PathVariable("namespace") namespace: String,
                      @PathVariable("collection") collection: String,
                      @PathVariable("version") version: String,
                      @PathVariable("userId") userId: String, 
                      @PathVariable("startIdx") startIdx: Int, 
                      @PathVariable("endIdx") endIdx: Int): String = {
    try{
      
      // TODO:  try (Jedis jedis = pool.getResource()) { }; pool.destroy();

      val results = new RecommendationsCommand("recommendations", jedisPool.getResource, namespace, version, userId, startIdx, endIdx)
       .execute()
      s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/api/v1/model/predict/keyvalue/{namespace}/{collection}/{version}/{itemId}/{startIdx}/{endIdx}"),
                  produces=Array("application/json; charset=UTF-8"))
  def similars(@PathVariable("namespace") namespace: String,
               @PathVariable("collection") collection: String,
               @PathVariable("version") version: String,
               @PathVariable("itemId") itemId: String, 
               @PathVariable("startIdx") startIdx: Int, 
               @PathVariable("endIdx") endIdx: Int): String = {
    try {
       val results = new ItemSimilarsCommand("item_similars", jedisPool.getResource, namespace, version, itemId, startIdx, endIdx)
         .execute()
       s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  -F "model=@tensorflow_inception_graph.pb" \
  //  http://[host]:[port]/api/v1/model/deploy/spark/[namespace]/[model_name]/[version]
  @RequestMapping(path=Array("/api/v1/model/deploy/spark/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def deploySpark(@PathVariable("namespace") namespace: String,
                  @PathVariable("modelName") modelName: String, 
                  @PathVariable("version") version: String,
                  @RequestParam("model") model: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      val filename = model.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"model_store/spark/${namespace}/${modelName}/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      inputStream = model.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"model_store/spark/${namespace}/${modelName}/${version}/${filename}"))
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

  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  -F "input=@input.json" \
  //  http://[host]:[port]/api/v1/model/predict/spark/[namespace]/[model_name]/[version]
  @RequestMapping(path=Array("/api/v1/model/predict/spark/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
    def predictSpark(@PathVariable("namespace") namespace: String,
                     @PathVariable("modelName") modelName: String,
                     @PathVariable("version") version: String,
                     @RequestBody inputJson: String): String = {
    try {
      val inputs = JSON.parseFull(inputJson).get.asInstanceOf[Map[String,Any]]
    
      val results = new SparkEvaluationCommand(modelName, namespace, modelName, version, inputs, "fallback", 5000, 20, 10)
          .execute()
  
      s"""{"results":[${results}]}"""
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
    }
  }  
}

object SparkModelGenerator {
  // TODO:
}

object JavaCodeGenerator {
  def codegen(sourceName: String, source: String): (Predictable, String) = {   
    val references = Map[String, Any]()

    val codeGenBundle = new CodeGenBundle(sourceName,
        null, 
        Array(classOf[Initializable], classOf[Predictable], classOf[Serializable]), 
        Array(classOf[java.util.HashMap[String, Any]], classOf[java.util.Map[String, Any]]), 
        CodeFormatter.stripExtraNewLines(source)
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
