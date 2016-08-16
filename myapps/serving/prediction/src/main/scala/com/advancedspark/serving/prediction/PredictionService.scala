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
import com.advancedspark.codegen.example.RecommendationMap
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

 var javaInstance: RecommendationMap = null

 @RequestMapping(path=Array("/update-java-source/{javaSourceName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def updateJavaSource(@PathVariable("javaSourceName") javaSourceName: String, @RequestBody javaSource: String): ResponseEntity[HttpStatus] = {
    try {
      //  TODO:  Compile JavaSource
      //  TODO:  Update JavaSource in Cache
      System.out.println("javaSource: ${javaSource}")

      javaInstance = RecommendationMapCodeGenerator.codegen(javaSourceName, javaSource).asInstanceOf[RecommendationMap]
      System.out.println(s"generated instance: ${javaInstance}")

      new ResponseEntity(HttpStatus.OK)
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }
 
  @RequestMapping(path=Array("/evaluate-java-source/{javaSourceName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluateJavaSource(@PathVariable("javaSourceName") javaSourceName: String, @RequestBody inputs: String): String = {
    try {
      //val inputMap = JSON.parseFull(inputs).get.asInstanceOf[Map[String,Any]]
      //System.out.println(inputMap)
  
      val key = "a"
      val value = javaInstance.getRecommendationsForUser(key)

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
    import org.jpmml.evaluator.TreeModelEvaluator
    import org.jpmml.evaluator.ModelEvaluatorFactory
    import org.jpmml.evaluator.Evaluator
    import org.jpmml.model.ImportFilter
    import org.jpmml.model.JAXBUtil

    try {
      /*
      val inputsMap: Map[String, _] = Map("age" -> 39,
                                          "workclass" -> "State-gov",
                                          "education" -> "Bachelors",
                                     	  "education_num" -> 13,
                                     	  "marital_status" -> "Never-married",
                                     	  "occupation" -> "Adm-clerical",
                                     	  "relationship" -> "Not-in-family",
                                     	  "race" -> "White",
                                     	  "sex" -> "Male",
                                     	  "capital_gain" -> 2174,
                                     	  "capital_loss" -> 0,
                                     	  "hours_per_week" -> 40,
                                     	  "native_country" -> "United-States")
      */

      val inputMap = JSON.parseFull(inputs).get.asInstanceOf[Map[String,Any]]

      val pmml: java.io.File = new java.io.File(s"data/${pmmlName}/${pmmlName}.pmml")

      // From the following:  https://github.com/jpmml/jpmml-evaluator
      val is = new java.io.FileInputStream(pmml.getAbsolutePath())
      val transformedSource = ImportFilter.apply(new InputSource(is))

      val pmml2 = JAXBUtil.unmarshalPMML(transformedSource)

      val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

      val modelEvaluator: Evaluator = modelEvaluatorFactory.newModelManager(pmml2)

      val activeFields = modelEvaluator.getActiveFields().asScala

      val arguments =
        ( for(activeField <- activeFields)
        // The raw value is passed through:
        //   1) outlier treatment,
        //   2) missing value treatment,
        //   3) invalid value treatment
        //   4) type conversion
          yield (activeField -> modelEvaluator.prepare(activeField, inputMap(activeField.getValue)))
        ).toMap.asJava

      val results = modelEvaluator.evaluate(arguments)
      val targetName = modelEvaluator.getTargetField()
      val targetValue = results.get(targetName)

      s"""{"results":[{'${targetName.getValue}': '${targetValue}'}]}"""
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

object RecommendationMapCodeGenerator {
  def codegen(javaSourceName: String, javaSource: String): RecommendationMap = {   
    //val ctx = new CodeGenContext()
    
    //ctx.addMutableState(JAVA_STRING, "str", "str = \"blahblah\";")

    //val lookupMap = new java.util.HashMap[Any, Any]()

    // TODO:  To lower the memory footprint, and improve cache locality, 
    //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
    //        
    // String :: primitive int array
    //lookupMap.put("a", (10001, 10002))
    //lookupMap.put("b", (10003, 10004))
    //lookupMap.put("c", (10005, 10006))
    
    //ctx.addReferenceObj("lookupMap", lookupMap, lookupMap.getClass.getName)

    //ctx.addNewFunction("lookup", "public Object lookup(Object key) { return lookupMap.get(key); }")
       
    // TODO:  Disable comments and line numbers as they're expensive
    // val source = s"""
    //  ${ctx.registerComment("LookupMap Comment...")}
    
    //  ${ctx.declareMutableStates()}

    //  public void initialize(Object[] references) {
    //    ${ctx.initMutableStates()}
    //  }

    //  ${ctx.declareAddedFunctions()}
    //  """.trim

    // Format and compile source
    // Note:  If you see "InstantiationException", you might be trying to create a package+classname that already exists.
    //        This is why we're namespacing this package to include ".generated", but we also changed the name of this
    //        outer class to LookupMapMain to make this more explicit.
    /////////////////////////////////////////////////
    // TODO:  Remove this!
     val recommendationMap = new java.util.HashMap[Any, Any]()

    // TODO:  To lower the memory footprint, and improve cache locality, 
    //        we can store the value list in a more-compressed fashion and avoid pointer-hopping which thrashes CPU caches.
    //        
    // String -> Array[String]
    recommendationMap.put("21619", ("10001", "10002"))
    recommendationMap.put("21620", ("10003", "10004"))
    recommendationMap.put("21621", ("10005", "10006"))
    
    val references = new scala.collection.mutable.ArrayBuffer[Any]()
    references += recommendationMap
    /////////////////////////////////////////////////

    val codeGenBundle = new CodeGenBundle("com.advancedspark.codegen.example.generated.RecommendationMap",        
        null, 
        Array(classOf[Initializable], classOf[RecommendationMap], classOf[Serializable]), 
        Array(classOf[java.util.Map[Any, Any]]), 
        CodeFormatter.stripExtraNewLines(javaSource)
    )
    
    try {
      val clazz = CodeGenerator.compile(codeGenBundle)
      System.out.println(s"\n${CodeFormatter.format(codeGenBundle)}")      
      
      //val references = ctx.references.toArray
      
      System.out.println("Instantiating and initializing with with generated class.")
      val bar = clazz.newInstance().asInstanceOf[Initializable]
      bar.initialize(references.toArray)

      System.out.println("Testing new instance.")
      System.out.println(s"Recommendations for '21619' -> '${bar.asInstanceOf[RecommendationMap].getRecommendationsForUser("21619")}'")

      System.out.println("Instantiating and initializing instance from parent classloader.")
      val clazz2 = clazz.getClassLoader.loadClass("com.advancedspark.codegen.example.generated.RecommendationMap")
      val bar2 = clazz2.newInstance().asInstanceOf[Initializable]
      bar2.initialize(references.toArray) 
      System.out.println(s"Recommendations for '21620' -> '${bar2.asInstanceOf[RecommendationMap].getRecommendationsForUser("21620")}'")

      bar2.asInstanceOf[RecommendationMap] 
    } catch {
      case e: Exception =>
        System.out.println(s"Could not generate code: ${codeGenBundle}", e)
        throw e
    }
  }
}
