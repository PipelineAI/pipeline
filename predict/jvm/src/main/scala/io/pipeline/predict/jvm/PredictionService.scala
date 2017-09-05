package io.pipeline.predict.jvm

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
import java.nio.file.Path

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("predict-jvm")
  new StandardExports().register()

  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]
  val predictorRegistry = new scala.collection.mutable.HashMap[String, Predictable]
  val modelRegistry = new scala.collection.mutable.HashMap[String, Array[Byte]]
  
  val redisHostname = "redis-master"
  val redisPort = 6379
 
  val jedisPool = new JedisPool(new JedisPoolConfig(), redisHostname, redisPort);

  val responseHeaders = new HttpHeaders();
  
/*
    curl -i -X POST -v -H "Content-Type: application/json" \
      -d '{"id":"21618"}' \
      http://[hostname]:[port]/api/v1/model/predict/java/default/java_equals/v0
*/
  @RequestMapping(path=Array("/api/v1/model/predict/java/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictJava(@PathVariable("modelName") modelName: String,                   
                  @RequestBody inputJson: String): 
      ResponseEntity[String] = {
    Try {
      val parentDir = s"/root/model/"

      val predictorOption = predictorRegistry.get(parentDir)
      
      val parsedInputOption = JSON.parseFull(inputJson)      
      
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val predictor = predictorOption match {
        case None => {
          val sourceFileName = s"${parentDir}/model.java"
          
          //read file into stream
          val stream: Stream[String] = Files.lines(Paths.get(sourceFileName))
			    
          // reconstuct original
          val source = stream.collect(Collectors.joining("\n"))
          
          val (predictor, generatedCode) = JavaCodeGenerator.codegen(modelName, source)        
      
          // Update Predictor in Cache
          predictorRegistry.put(parentDir, predictor)

          predictor
        }
        case Some(predictor) => {
           predictor
        }
      } 
          
      val result = new JavaSourceCodeEvaluationCommand("java", modelName, predictor, inputs, "fallback", 25, 20, 10).execute()

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
 
  @RequestMapping(path=Array("/api/v1/model/predict/pmml/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictPmml(@PathVariable("modelName") modelName: String, 
                  @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val parentDir = s"/root/model/"
            
      val modelEvaluatorOption = pmmlRegistry.get(parentDir)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          
          // TODO:  Make sure the bundle contains a file called model.pmml!

          val fis = new java.io.FileInputStream(s"${parentDir}/model.pmml")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(parentDir, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand("pmml", modelName, modelEvaluator, inputs, "\"fallback\"", 100, 20, 10).execute()

      s"""{"outputs":${results}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }  
  
 @RequestMapping(path=Array("/api/v1/model/predict/r/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictR(@PathVariable("modelName") modelName: String, 
               @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val parentDir = s"/root/model/"
            
      val modelEvaluatorOption = pmmlRegistry.get(parentDir)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          
          // TODO:  Make sure the bundle contains a file called model.r!
          
          val fis = new java.io.FileInputStream(s"${parentDir}/model.r")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(parentDir, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand("r", modelName, modelEvaluator, inputs, "\"fallback\"", 100, 20, 10).execute()

      s"""{"outputs":${results}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }    
   
  @RequestMapping(path=Array("/api/v1/model/predict/xgboost/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def predictXgboost(@PathVariable("modelName") modelName: String,                      
                     @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val parentDir = s"/root/model/"
      
      val modelEvaluatorOption = pmmlRegistry.get(parentDir)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {   
          
          // TODO:  Make sure the bundle contains a file called model.xgboost!
          
          val fis = new java.io.FileInputStream(s"${parentDir}/model.xgboost")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(parentDir, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      val results = new PMMLEvaluationCommand("xgboost", modelName, modelEvaluator, inputs, "\"fallback\"", 100, 20, 10).execute()

      s"""{"results":[${results}]}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }    
 
  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  http://[host]:[port]/api/v1/model/predict/spark/[namespace]/[model_name]/[version]
  @RequestMapping(path=Array("/api/v1/model/predict/spark/{modelName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
    def predictSpark(@PathVariable("modelName") modelName: String,                     
                     @RequestBody inputJson: String): String = {
    try {
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val parentDir = s"/root/model/"
      
      val modelEvaluatorOption = pmmlRegistry.get(parentDir)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          val fis = new java.io.FileInputStream(s"${parentDir}/model.spark")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(parentDir, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }                 
      
      val results = new PMMLEvaluationCommand("spark", modelName, modelEvaluator, inputs, "\"fallback\"", 100, 20, 10).execute()

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
      val result = new UserItemPredictionCommand("keyvalue_useritem", namespace, version, -1.0d, 25, 5, 10, userId, itemId)           
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
      val result = new UserItemBatchPredictionCollapser("keyvalue_useritem_batch", namespace, version, -1.0d, 25, 5, 10, userId, itemId)
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

      System.out.println(s"Generated code: \n${generatedCode}}")      
            
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
