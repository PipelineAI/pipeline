package com.advancedspark.serving.prediction

import org.springframework.boot._
import org.springframework.boot.autoconfigure._
import org.springframework.stereotype._
import org.springframework.web.bind.annotation._
import org.springframework.cloud.context.config.annotation._
import org.springframework.boot.context.embedded._
import org.springframework.context.annotation._
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import scala.util.parsing.json._

import scala.collection.JavaConversions._
import java.util.Collections
import java.util.Collection
import java.util.Set
import java.util.List

import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import scala.reflect.BeanProperty

import com.netflix.dyno.jedis._
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.connectionpool.exception.DynoException
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.dyno.connectionpool.impl.ConnectionContextImpl
import com.netflix.dyno.connectionpool.impl.OperationResultImpl
import com.netflix.dyno.connectionpool.impl.utils.ZipUtils

import scala.collection.JavaConverters._

import com.advancedspark.codegen.CodeFormatter
import com.advancedspark.codegen.CodeGenBundle
import com.advancedspark.codegen.CodeGenContext
import com.advancedspark.codegen.CodeGenTypes._
import com.advancedspark.codegen.CodeGenerator
import com.advancedspark.codegen.DumpByteCode
import com.advancedspark.codegen.example.Lookupable
import com.advancedspark.codegen.example.Initializable

@SpringBootApplication
@RestController
@EnableHystrix
@EnableEurekaClient
@Configuration
@Bean
@RefreshScope
class PredictionService {
  @Value("${prediction.namespace:''}")
  val namespace = ""

  @Value("${prediction.version:''}")
  val version = "" 

  @Value("${prediction.census.pmml:'blah.pmml'}")
  val predictionCensusPmml= ""

  @RequestMapping(path=Array("/prediction/{userId}/{itemId}"),  
                  produces=Array("application/json; charset=UTF-8"))
  def prediction(@PathVariable("userId") userId: String, @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemPredictionCommand(PredictionServiceOps.dynoClient, namespace, version, userId, itemId)
        .execute()
      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/batch-prediction/{userIds}/{itemIds}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchPrediction(@PathVariable("userIds") userIds: Array[String], @PathVariable("itemIds") itemIds: Array[String]): String = {
    try {
      val result = new UserItemBatchPredictionCommand(PredictionServiceOps.dynoClient, namespace, version, userIds, itemIds)
        .execute()
      s"""{"result":${result.mkString(",")}}"""
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/recommendations/{userId}/{startIdx}/{endIdx}"), 
                  produces=Array("application/json; charset=UTF-8"))
  def recommendations(@PathVariable("userId") userId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try{
      val results = new UserItemRecommendationsCommand(PredictionServiceOps.dynoClient, namespace, version, userId, startIdx, endIdx)
       .execute()
      s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/similars/{itemId}/{startIdx}/{endIdx}"),
                  produces=Array("application/json; charset=UTF-8"))
  def similars(@PathVariable("itemId") itemId: String, @PathVariable("startIdx") startIdx: Int, 
      @PathVariable("endIdx") endIdx: Int): String = {
    try {
       val results = new ItemSimilarsCommand(PredictionServiceOps.dynoClient, namespace, version, itemId, startIdx, endIdx)
         .execute()
       s"""{"results":[${results.mkString(",")}]}"""
    } catch {
       case e: Throwable => {
         System.out.println(e)
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
         System.out.println(e)
         throw e
       }
    }
  }
  */

  var javaInstance: Lookupable = null

  @RequestMapping(path=Array("/update-lookup/{className}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateLookup(@PathVariable("className") className: String, @RequestBody classSource: String): ResponseEntity[HttpStatus] = {
    try {
      //  TODO:  Update Cache
      System.out.println("updating ${className}: ${classSource}")

      /////////////////////////////////////
      // TODO:  Remove this in favor of a json-based map passed in through the payload?
      val lookupMap = new java.util.HashMap[Any, Any]()

      // TODO:  To lower the memory footprint, and improve cache locality,
      //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
      //
      // String -> Array[String]
      lookupMap.put("21619", ("10001", "10002"))
      lookupMap.put("21620", ("10003", "10004"))
      lookupMap.put("21621", ("10005", "10006"))
      /////////////////////////////////////

      javaInstance = LookupableCodeGenerator.codegen(className, classSource, lookupMap).asInstanceOf[Lookupable]

 //     val classLoader = new org.codehaus.janino.ByteArrayClassLoader(Map(className -> classBody))
 //     javaInstance = classLoader.loadClass(className, true).newInstance().asInstanceOf[Lookupable]
      System.out.println(s"javaInstance: ${javaInstance}")

      new ResponseEntity(HttpStatus.OK)
    } catch {
      case e: Throwable => {
        System.out.println(e)
        throw e
      }
     }
  }
 
  @RequestMapping(path=Array("/evaluate-lookup/{className}/{key}"),
                  method=Array(RequestMethod.GET),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateLookup(@PathVariable("className") className: String, @PathVariable("key") key: String): String = {
    try {
      //val inputMap = JSON.parseFull(inputs).get.asInstanceOf[Map[String,Any]]
      //System.out.println(inputMap)
  
     // val key = "21619"
      val value = javaInstance.lookup(key)

      s"""{"results":[{'${key}': '${value}'}]}"""
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/update-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody pmml: String): ResponseEntity[HttpStatus] = {
    try {
      //  TODO:  Create Evaluator from pmml
      //  TODO:  Update pmml Evaluator in Cache
      
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
  
      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/evaluate-pmml/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody inputs: String): String = {
    System.out.println("Prediction Census PMML: " + predictionCensusPmml)

    import java.io.FileInputStream
    import org.jpmml.model.MetroJAXBUtil
    import org.xml.sax.InputSource
    import org.jpmml.evaluator.ModelEvaluatorFactory
    import org.jpmml.evaluator.Evaluator
    import org.jpmml.model.ImportFilter
    import org.jpmml.model.JAXBUtil

    try {
      val inputsMap = JSON.parseFull(inputs).get.asInstanceOf[Map[String,Any]]

      val pmml: java.io.File = new java.io.File(s"data/${pmmlName}/${pmmlName}.pmml")

      // From the following:  https://github.com/jpmml/jpmml-evaluator
      val is = new java.io.FileInputStream(pmml.getAbsolutePath())
      val transformedSource = ImportFilter.apply(new InputSource(is))

      val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)

      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelEvaluator(pmml2)
      System.out.println("Mining function: " + modelEvaluator.getMiningFunction())

      val inputFields = modelEvaluator.getInputFields().asScala

      System.out.println("Input schema:");
      System.out.println("\t" + "Input fields: " + inputFields)
//    System.out.println("\t" + "Group fields: " + modelEvaluator.getGroupFields())

      System.out.println("Output schema:");
      System.out.println("\t" + "Target fields: " + modelEvaluator.getTargetFields())
      System.out.println("\t" + "Output fields: " + modelEvaluator.getOutputFields())

      val arguments =
        ( for(inputField <- inputFields)
          // The raw value is passed through:
          //   1) outlier treatment,
          //   2) missing value treatment,
          //   3) invalid value treatment
          //   4) type conversion
          yield (inputField.getName -> inputField.prepare(inputsMap(inputField.getName.getValue)))
        ).toMap.asJava

      System.out.println(arguments)

      val results = modelEvaluator.evaluate(arguments)
      val targetField = modelEvaluator.getTargetFields().asScala(0)
      val targetValue = results.get(targetField.getName)

      s"""{"results":[{'${targetField.getName}': '${targetValue}'}]}"""
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

//@Configuration
object PredictionServiceOps {
  @Value("${prediction.redis.host:127.0.0.1}")
  val hostname = "127.0.0.1"

  val host = new Host(hostname, Host.Status.Up)

  val hostSupplier = new HostSupplier() {
    @Override
    def getHosts(): Collection[Host] = {
      Collections.singletonList(host)
    }
  }

  val hostToken = new HostToken(100000L, host)
  val tokenMapSupplier = new TokenMapSupplier() {
    @Override
    def getTokens(activeHosts: Set[Host]): List[HostToken] = {
      Collections.singletonList(hostToken)
    }

    @Override
    def getTokenForHost(host: Host, activeHosts: Set[Host]): HostToken = {
      return hostToken
    }
  }

  @Value("${prediction.redis.port:6379}")
  val port = 6379
  val dynoClient = new DynoJedisClient.Builder()
             .withApplicationName("pipeline")
             .withDynomiteClusterName("pipeline-dynomite")
             .withHostSupplier(hostSupplier)
             .withCPConfig(new ConnectionPoolConfigurationImpl("tokenMapSupplier")
                .withTokenSupplier(tokenMapSupplier))
             .withPort(port)
             .build()
}

object LookupableCodeGenerator {
  def codegen(className: String, classSource: String, lookupMap: java.util.Map[Any, Any]): Lookupable = {   
    
    val references = new scala.collection.mutable.ArrayBuffer[Any]()
    references += lookupMap 
    /////////////////////////////////////////////////

    val codeGenBundle = new CodeGenBundle("com.advancedspark.codegen.example.generated.LookupMap",        
        null, 
        Array(classOf[Initializable], classOf[Lookupable], classOf[Serializable]), 
        Array(classOf[java.util.Map[Any, Any]]), 
        CodeFormatter.stripExtraNewLines(classSource)
    )
    
    try {
      val clazz = CodeGenerator.compile(codeGenBundle)
      System.out.println(s"\n${CodeFormatter.format(codeGenBundle)}")      
      
      //val references = ctx.references.toArray
      
      System.out.println("Instantiating and initializing with with generated class.")
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references.toArray)

      System.out.println("Testing new instance.")
      System.out.println(s"Lookup for '21619' -> '${bar.asInstanceOf[Lookupable].lookup("21619")}'")

      System.out.println("Instantiating and initializing instance from parent classloader.")
      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.LookupMap")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(references.toArray) 
      System.out.println(s"Lookup for '21620' -> '${bar2.asInstanceOf[Lookupable].lookup("21620")}'")

      bar2.asInstanceOf[Lookupable] 
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${codeGenBundle}", e)
        throw e
    }
  }
}
