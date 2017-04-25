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
import org.springframework.web.bind.annotation.RequestMethod

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

  @RequestMapping(path=Array("/v1/model/update/java/{namespace}/{sourceName}/{version}"),
                  method=Array(RequestMethod.POST)
                  //produces=Array("application/json; charset=UTF-8")
                  )
  def updateSource(@PathVariable("namespace") namespace: String, 
                   @PathVariable("sourceName") sourceName: String,
                   @PathVariable("version") version: String,
                   @RequestBody source: String): 
      ResponseEntity[String] = {
    Try {
      System.out.println(s"Generating source for ${namespace}/${sourceName}/${version}:\n${source}")

      // Write the new java source to local disk
      val path = new java.io.File(s"store/${namespace}/${sourceName}/${version}")
      if (!path.isDirectory()) {
        path.mkdirs()
      }

      val file = new java.io.File(s"store/${namespace}/${sourceName}/${version}/${sourceName}.java")
      if (!file.exists()) {
        file.createNewFile()
      }

      val fos = new java.io.FileOutputStream(file)
      fos.write(source.getBytes())

      val (predictor, generatedCode) = PredictorCodeGenerator.codegen(sourceName, source)
      
      System.out.println(s"Updating cache for ${namespace}/${sourceName}/${version}:\n${generatedCode}")
      
      // Update Predictor in Cache
      predictorRegistry.put(namespace + "/" + sourceName + "/" + version, predictor)

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
  
/*
    curl -i -X POST -v -H "Content-Type: application/json" \
      -d {"id":"21618"} \
      http://[hostname]:[port]/v1/model/predict/java/default/java_equals/1
*/
  @RequestMapping(path=Array("/v1/model/predict/java/{namespace}/{sourceName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictJava(@PathVariable("namespace") namespace: String, 
                     @PathVariable("sourceName") sourceName: String, 
                     @PathVariable("version") version: String,
                     @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
      val predictorOption = predictorRegistry.get(namespace + "/" + sourceName + "/" + version)

      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val predictor = predictorOption match {
        case None => {
          val sourceFileName = s"store/${namespace}/${sourceName}/${version}/${sourceName}.java"

          //read file into stream
          val stream: Stream[String] = Files.lines(Paths.get(sourceFileName))
			    
          // reconstuct original
          val source = stream.collect(Collectors.joining("\n"))
          
          val (predictor, generatedCode) = PredictorCodeGenerator.codegen(sourceName, source)

          System.out.println(s"Updating cache for ${namespace}/${sourceName}/${version}:\n${generatedCode}")
      
          // Update Predictor in Cache
          predictorRegistry.put(namespace + "/" + sourceName + "/" + version, predictor)
      
          System.out.println(s"Updating cache for ${namespace}/${sourceName}/${version}:\n${generatedCode}")

          predictor
        }
        case Some(predictor) => {
           predictor
        }
      } 
          
      val result = new JavaSourceCodeEvaluationCommand(sourceName, namespace, sourceName, version, predictor, inputs, "fallback", 25, 20, 10).execute()

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

  @RequestMapping(path=Array("/v1/model/update/pmml/{namespace}/{pmmlName}/{version}"),
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

  @RequestMapping(path=Array("/v1/model/predict/pmml/{namespace}/{pmmlName}/{version}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictPmml(@PathVariable("namespace") namespace: String, 
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
  
  @RequestMapping(path=Array("/v1/model/predict/keyvalue/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def prediction(@PathVariable("namespace") namespace: String,
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
  
  @RequestMapping(path=Array("/v1/model/predict/keyvalue/batch/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchPrediction(@PathVariable("namespace") namespace: String,
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
  
  @RequestMapping(path=Array("/v1/model/predict/recommendations/{namespace}/{collection}/{version}/{userId}/{startIdx}/{endIdx}"), 
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

  @RequestMapping(path=Array("/v1/model/predict/similars/{namespace}/{collection}/{version}/{itemId}/{startIdx}/{endIdx}"),
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
  //  http://[host]:[port]/v1/model/update/spark/[namespace]/[model_name]/[version]
  @RequestMapping(path=Array("/v1/model/update/spark/{namespace}/{modelName}/{version}"),
                  method=Array(RequestMethod.POST))
  def updateSpark(@PathVariable("namespace") namespace: String,
                  @PathVariable("modelName") modelName: String, 
                  @PathVariable("version") version: String,
                  @RequestParam("model") model: MultipartFile): ResponseEntity[HttpStatus] = {

    var inputStream: InputStream = null

    try {
      // Get name of uploaded file.
      val filename = model.getOriginalFilename()
  
      // Path where the uploaded file will be stored.
      val filepath = new java.io.File(s"store/${namespace}/${modelName}/${version}")
      if (!filepath.isDirectory()) {
        filepath.mkdirs()
      }
  
      // This buffer will store the data read from 'model' multipart file
      inputStream = model.getInputStream()
  
      Files.copy(inputStream, Paths.get(s"store/${namespace}/${modelName}/${version}/${filename}"))
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
  //  http://[host]:[port]/v1/model/predict/spark/[namespace]/[model_name]/[version]
  @RequestMapping(path=Array("/v1/model/predict/spark/{namespace}/{modelName}/{version}"),
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

object PredictorCodeGenerator {
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
