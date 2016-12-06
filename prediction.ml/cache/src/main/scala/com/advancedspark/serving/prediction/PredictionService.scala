package com.advancedspark.serving.prediction

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
import org.springframework.web.bind.annotation._

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes._
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.Predictable
import com.advancedspark.codegen.Initializable

import redis.clients.jedis._

@SpringBootApplication
@RestController
@EnableHystrix
//@EnableEurekaClient
//@Configuration
//@RefreshScope
class PredictionService {
  val namespace = ""

  val version = "" 

  val redisHostname = "redis-master"

  val redisPort = 6379
  
  val jedisPool = new JedisPool(new JedisPoolConfig(), redisHostname, redisPort);

  @RequestMapping(path=Array("/prediction/{userId}/{itemId}"),  
                  produces=Array("application/json; charset=UTF-8"))
  def prediction(@PathVariable("userId") userId: String, @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemPredictionCommand(jedisPool.getResource, namespace, version, userId, itemId)
        .execute()
      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/batch-prediction/{userIds}/{itemIds}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchPrediction(@PathVariable("userIds") userIds: Array[String], @PathVariable("itemIds") itemIds: Array[String]): String = {
    try {
      val result = new UserItemBatchPredictionCommand(jedisPool.getResource, namespace, version, userIds, itemIds)
        .execute()
      s"""{"result":${result.mkString(",")}}"""
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/recommendations/{userId}/{startIdx}/{endIdx}"), 
                  produces=Array("application/json; charset=UTF-8"))
  def recommendations(@PathVariable("userId") userId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try{
      
      // TODO:  try (Jedis jedis = pool.getResource()) { }; pool.destroy();

      val results = new RecommendationsCommand(jedisPool.getResource, namespace, version, userId, startIdx, endIdx)
       .execute()
      s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/similars/{itemId}/{startIdx}/{endIdx}"),
                  produces=Array("application/json; charset=UTF-8"))
  def similars(@PathVariable("itemId") itemId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try {
       val results = new ItemSimilarsCommand(jedisPool.getResource, namespace, version, itemId, startIdx, endIdx)
         .execute()
       s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }

  /*
   * TODO: Proxy this through to TensorFlow
  @RequestMapping(path=Array("/image-classify/{itemId}"),
                  produces=Array("application/json; charset=UTF-8")) 
  def imageClassify(@PathVariable("itemId") itemId: String): String = {
    // TODO:
    try {
      // TODO:  Convert to JSON
      Map("Pandas" -> 1.0).toString
    } catch {
       case e: Throwable => {
 //        System.out.println(e)
         throw e
       }
    }
  }
  */

  var predictor: Predictable = null
  
  @RequestMapping(path=Array("/update-source/{className}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateSource(@PathVariable("className") className: String, @RequestBody classSource: String): ResponseEntity[HttpStatus] = {
    try {
      //  TODO:  Update Cache
      System.out.println("updating ${className}: ${classSource}")

      /////////////////////////////////////
      // TODO:  Remove this in favor of a json-based map passed in through the payload?
      val predictions = new java.util.HashMap[Any, Any]()

      // TODO:  To lower the memory footprint, and improve cache locality,
      //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
      //
      // String -> Array[String]
      predictions.put("21619", ("10001", "10002"))
      predictions.put("21620", ("10003", "10004"))
      predictions.put("21621", ("10005", "10006"))
      /////////////////////////////////////
      
      try {
        predictor = PredictorCodeGenerator.codegen(className, classSource, predictions).asInstanceOf[Predictable]  
      } catch {
         case e: Throwable => {
//           System.out.println(e)
           throw e
         }
      }
      
      System.out.println(s"predictor: ${predictor}")

      new ResponseEntity(HttpStatus.OK)
    } catch {
      case e: Throwable => {
//        System.out.println(e)
        throw e
      }
     }
  }
 
  @RequestMapping(path=Array("/evaluate-source/{className}/{key}"),
                  method=Array(RequestMethod.GET),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateSource(@PathVariable("className") className: String, @PathVariable("key") key: String): String = {
    try {

      val result = new SourceCodeEvaluationCommand(predictor, key)
         .execute()      

      s"""{"results":[{'${key}': '${result}'}]}"""
    } catch {
       case e: Throwable => {
  //       System.out.println(e)
         throw e
       }
    }
  }
  
  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]

  @RequestMapping(path=Array("/update-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody pmml: String): ResponseEntity[HttpStatus] = {
    try {
      // Write the new pmml (XML format) to local disk
      val path = new java.io.File(s"data/${pmmlName}/")
      if (!path.isDirectory()) { 
        path.mkdirs()
      }

      val file = new java.io.File(s"data/${pmmlName}/${pmmlName}.pmml")
      if (!file.exists()) {
        file.createNewFile()
      }

      val os = new java.io.FileOutputStream(file)
      os.write(pmml.getBytes())    

 //     System.out.println(s"""pmmlName: $pmmlName""")
 //     System.out.println(s"""pmml: $pmml""")

      val transformedSource = ImportFilter.apply(new InputSource(new java.io.StringReader(pmml)))

      val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)

      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml2)

      // Update PMML in Cache
      pmmlRegistry.put(pmmlName, modelEvaluator)
      
      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/evaluate-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody inputs: String): String = {

    try {
      var modelEvaluator: Evaluator = null 
      val modelEvaluatorOption = pmmlRegistry.get(pmmlName)
      if (modelEvaluatorOption == None) {
        val is = new java.io.FileInputStream(s"data/${pmmlName}/${pmmlName}.pmml")
        val transformedSource = ImportFilter.apply(new InputSource(is))

        val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)

        val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

        modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml2)

        // Cache modelEvaluator
        pmmlRegistry.put(pmmlName, modelEvaluator)
      } else {
        modelEvaluator = modelEvaluatorOption.get
      }

      val results = new PMMLEvaluationCommand(modelEvaluator, inputs)
       .execute()

      s"""{"results":[${results}]"""
    } catch {
       case e: Throwable => {
//         System.out.println(e)
         throw e
       }
    }
  }
}

object PredictorCodeGenerator {
  def codegen(className: String, classSource: String, lookupMap: java.util.Map[Any, Any]): Predictable = {   
    
    val references = new scala.collection.mutable.ArrayBuffer[Any]()
    references += lookupMap 
    /////////////////////////////////////////////////

    val codeGenBundle = new CodeGenBundle("com.advancedspark.codegen.example.generated.Predictor",
        null, 
        Array(classOf[Initializable], classOf[Predictable], classOf[Serializable]), 
        Array(classOf[java.util.Map[Any, Any]]), 
        CodeFormatter.stripExtraNewLines(classSource)
    )
    
    try {
      val clazz = CodeGenerator.compile(codeGenBundle)
      System.out.println(s"\n${CodeFormatter.format(codeGenBundle)}")      
            
      System.out.println("Instantiating and initializing with with generated class.")
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references.toArray)

      System.out.println("Testing new instance.")
      System.out.println(s"Prediction for '21619' -> '${bar.asInstanceOf[Predictable].predict("21619")}'")

      System.out.println("Instantiating and initializing instance from parent classloader.")
      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.Predictor")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(references.toArray) 
      System.out.println(s"Prediction for '21620' -> '${bar2.asInstanceOf[Predictable].predict("21620")}'")

      bar2.asInstanceOf[Predictable] 
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${codeGenBundle}", e)
        throw e
    }
  }
}

object PredictionServiceMain {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[PredictionService])
  }
}
