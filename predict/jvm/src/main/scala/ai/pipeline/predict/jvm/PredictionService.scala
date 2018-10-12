package ai.pipeline.predict.jvm

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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMethod
import org.xml.sax.InputSource

import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher

import io.prometheus.client.hotspot.StandardExports
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.nio.file.StandardCopyOption
import java.nio.file.Path

@SpringBootApplication
@RestController
@EnableHystrix
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector	
class PredictionService {
  HystrixPrometheusMetricsPublisher.register("predictjvm")
  new StandardExports().register()

  // Model Runtimes
  val RUNTIME_PYTHON = "python"
  val RUNTIME_TFSERVING = "tfserving"
  val RUNTIME_JVM = "jvm"
  val RUNTIME_TFLIST = "tflite"

  // Model Types
  //   TODO: Sync These Types with Rest of Codebase
  val TYPE_SPARK = "spark"
  val TYPE_R = "r"
  val TYPE_PMML = "pmml"
  val TYPE_SCIKIT = "scikit"
  val TYPE_KERAS = "keras"
  val TYPE_JAVA = "java"
  val TYPE_PYTHON = "python"
  val TYPE_BASH = "bash"
  val TYPE_CAFFE = "caffe"
  val TYPE_MXNET = "mxnet"
  val TYPE_PYTORCH = "pytorch"
  val TYPE_TENSORFLOW = "tensorflow"
  val TYPE_XGBOOST = "xgboost"

  val pmmlRegistry = new scala.collection.mutable.HashMap[String, Evaluator]
  val predictorRegistry = new scala.collection.mutable.HashMap[String, Predictable]
  val modelRegistry = new scala.collection.mutable.HashMap[String, Array[Byte]]
  
  val redisHostname = "redis-master"
  val redisPort = 6379
 
  val jedisPool = new JedisPool(new JedisPoolConfig(), redisHostname, redisPort);

  val responseHeaders = new HttpHeaders();
 
  val modelRuntime = System.getenv("PIPELINE_RUNTIME")
  val modelChip = System.getenv("PIPELINE_CHIP")
  val modelType = System.getenv("PIPELINE_RESOURCE_SUBTYPE")
  val modelName = System.getenv("PIPELINE_RESOURCE_NAME")
  val modelTag = System.getenv("PIPELINE_RESOURCE_TAG")

  val httpProtocol = "http"
  val httpHost = "localhost"
  val httpPort = System.getenv("PIPELINE_RESOURCE_SERVER_PORT")
  val httpRequestMethod = "POST"

  // Pull in the following for Command/Circuit Fallbacks and Timeouts and Pool Sizes
  val fallbackString: String = scala.sys.env.getOrElse("PIPELINE_RESOURCE_SERVER_FALLBACK_STRING", "Fallback!")
  val timeoutMillis: Int = scala.sys.env.getOrElse("PIPELINE_RESOURCE_SERVER_TIMEOUT_MILLISECONDS", "5000").toInt
  val concurrencyPoolSizeNumThreads: Int = scala.sys.env.getOrElse("PIPELINE_RESOURCE_SERVER_CONCURRENCY_THREADS", "20").toInt
  val rejectionThresholdNumRejections: Int = scala.sys.env.getOrElse("PIPELINE_RESOURCE_SERVER_THRESHOLD_REJECTIONS", "10").toInt

  @RequestMapping(path=Array("/invoke"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def invoke(@RequestBody httpRequestBody: String,
              @RequestHeader(name="x-request-id", required=false) xreq: String,
              @RequestHeader(name="x-b3-traceid", required=false) xtraceid: String,
              @RequestHeader(name="x-b3-spanid", required=false) xspanid: String,
              @RequestHeader(name="x-b3-parentspanid", required=false) xparentspanid: String,
              @RequestHeader(name="x-b3-sampled", required=false) xsampled: String,
              @RequestHeader(name="x-b3-flags", required=false) xflags: String,
              @RequestHeader(name="x-ot-span-context", required=false) xotspan: String)
      : ResponseEntity[String] = {

    // TODO:  Pass the ZipKin/Jaeger Headers through all of these methods.
    //        The python code should be instrumented, as well.

    Try {
      modelRuntime match {
        case RUNTIME_JVM => {
          modelType match {
             case TYPE_SPARK => invokeSpark(modelChip, modelName, modelTag, httpRequestBody)
             case TYPE_PMML => invokePmml(modelChip, modelName, modelTag, httpRequestBody)
             case TYPE_JAVA => invokeJava(modelChip, modelName, modelTag, httpRequestBody)
             // case TYPE_R => invokeR(modelChip, modelName, modelTag, httpRequestBody)
             // case TYPE_XGBOOST => invokeXgboost(modelChip, modelName, modelTag, httpRequestBody)
          }
        }
        case _ => {
          // TODO:  Add the ZipKin Headers to this HttpEvaluationCommand
          new HttpEvaluationCommand(s"""${httpProtocol}://${httpHost}:${httpPort}/""",
                                                   modelName,
                                                   modelTag,
                                                   modelType,
                                                   modelRuntime,
                                                   modelChip,
                                                   httpRequestMethod,
                                                   httpRequestBody,
                                                   fallbackString,
                                                   timeoutMillis, // 5000
                                                   concurrencyPoolSizeNumThreads, // 20
                                                   rejectionThresholdNumRejections, // 10
                                                   xreq,
                                                   xtraceid,
                                                   xspanid,
                                                   xparentspanid,
                                                   xsampled,
                                                   xflags,
                                                   xotspan
                                   ).execute()
        }
      }
    }
    match {
      case Failure(t: Throwable) => {
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders,
          HttpStatus.BAD_REQUEST)
      }
      case Success(results) => {
        val variant = s"""${modelName + "-" + modelTag + "-" + modelType + "-" + modelRuntime + "-" + modelChip}"""
        new ResponseEntity[String](s"""{"variant": "${variant}", "outputs":${results}}""", responseHeaders, 
          HttpStatus.OK)
      }
    }
  }

  // This assumes /ping/jvm => /ping in nginx config
  @RequestMapping(path=Array("/ping"),
                  method=Array(RequestMethod.GET))
  def ping(): ResponseEntity[String] = {
    Try {
      s"""{"status":"OK"}"""
    } match {
      case Failure(t: Throwable) => {
        new ResponseEntity[String](s"""${t.getMessage}:\n${t.getStackTrace().mkString("\n")}""", responseHeaders, 
          HttpStatus.BAD_REQUEST)
      }
      case Success(results) => new ResponseEntity[String](results, responseHeaders, HttpStatus.OK)
    }
  }
	
  // From here down, we don't need the @RequestMapping's anymore (or @PathVariable)
  // We may need to preserve zipkin/jaeger tracing headers, however.
  // https://github.com/istio/istio.github.io/blob/a6fae1b3683e20e40a718f0bd2332350d8c7a87a/_docs/tasks/telemetry/distributed-tracing.md
  @RequestMapping(path=Array("/blah/blah/blah/see/above/comment/api/v1/model/invoke/jvm/{modelChip}/jvm/{modelName}/{modelTag}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def invokeJava(@PathVariable("modelChip") modelChip: String,
                  @PathVariable("modelName") modelName: String,
                  @PathVariable("modelTag") modelTag: String,
                  @RequestBody inputJson: String): String = {

      val modelRuntime = "jvm"
      val modelType = "java"

      val modelPath =  System.getenv("PIPELINE_RESOURCE_PATH")

      val predictorOption = predictorRegistry.get(modelPath)
      
      val parsedInputOption = JSON.parseFull(inputJson)      
      
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val predictor = predictorOption match {
        case None => {
          val sourceFileName = s"${modelPath}/pipeline_invoke.java"
          
          //read file into stream
          val stream: Stream[String] = Files.lines(Paths.get(sourceFileName))
			    
          // reconstuct original
          val source = stream.collect(Collectors.joining("\n"))
          
          val (predictor, generatedCode) = JavaCodeGenerator.codegen(modelName, source)        
      
          // Update Predictor in Cache
          predictorRegistry.put(modelPath, predictor)

          predictor
        }
        case Some(predictor) => {
           predictor
        }
      } 
          
      new JavaSourceCodeEvaluationCommand(modelName, 
                                          modelTag, 
                                          modelType, 
                                          modelRuntime, 
                                          modelChip, 
                                          predictor, 
                                          inputs, 
                                          fallbackString, 
                                          timeoutMillis, // 25 
                                          concurrencyPoolSizeNumThreads, // 20 
                                          rejectionThresholdNumRejections // 10
                                         ).execute()
  }
 
  @RequestMapping(path=Array("/blah/blah/blah/this/doesnt/matter/api/v1/model/predict/jvm/{modelChip}/pmml/{modelName}/{modelTag}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def invokePmml(@PathVariable("modelChip") modelChip: String,
                  @PathVariable("modelName") modelName: String,
                  @PathVariable("modelTag") modelTag: String,
                  @RequestBody inputJson: String): String = {

      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
      
      val modelRuntime = "jvm"
      val modelType = "pmml"

      val modelPath =  System.getenv("PIPELINE_RESOURCE_PATH")    
      val modelEvaluatorOption = pmmlRegistry.get(modelPath)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          
          // TODO:  Make sure the bundle contains a file called pipeline_invoke.pmml!

          val fis = new java.io.FileInputStream(s"${modelPath}/pipeline_invoke.pmml")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(modelPath, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      new PMMLEvaluationCommand(modelName, 
                                modelTag, 
                                modelType, 
                                modelRuntime, 
                                modelChip, 
                                modelEvaluator, 
                                inputs, 
                                fallbackString, 
                                timeoutMillis, // 100 
                                concurrencyPoolSizeNumThreads, // 20 
                                rejectionThresholdNumRejections // 10

          ).execute()
  }
  
 @RequestMapping(path=Array("/blah/blah/blah//api/v1/model/invoke/jvm/{modelChip}/r/{modelName}/{modelTag}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def invokeR(@PathVariable("modelChip") modelChip: String,
               @PathVariable("modelName") modelName: String, 
               @PathVariable("modelTag") modelTag: String,
               @RequestBody inputJson: String): String = {

      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val modelRuntime = "jvm"
      val modelType = "r"
      
      val modelPath =  System.getenv("PIPELINE_RESOURCE_PATH")    
      val modelEvaluatorOption = pmmlRegistry.get(modelPath)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          
          // TODO:  Make sure the bundle contains a file called model.r!
          
          val fis = new java.io.FileInputStream(s"${modelPath}/model.r")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(modelPath, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      new PMMLEvaluationCommand(modelName, 
                                modelTag, 
                                modelType, 
                                modelRuntime, 
                                modelChip,
                                modelEvaluator, 
                                inputs, 
                                fallbackString, 
                                timeoutMillis, // 100 
                                concurrencyPoolSizeNumThreads, // 20 
                                rejectionThresholdNumRejections // 10
                                ).execute()
  }    
   
  @RequestMapping(path=Array("/api/v1/model/invoke/jvm/{modelChip}/xgboost/{modelName}/{modelTag}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def invokeXgboost(@PathVariable("modelChip") modelChip: String,
                     @PathVariable("modelName") modelName: String,
                     @PathVariable("modelTag") modelTag: String,
                     @RequestBody inputJson: String): String = {

      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }
     
      val modelRuntime = "jvm"
      val modelType = "xgboost"
 
      val modelPath =  System.getenv("PIPELINE_RESOURCE_PATH")
      val modelEvaluatorOption = pmmlRegistry.get(modelPath)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {   
          
          // TODO:  Make sure the bundle contains a file called pipeline_invoke.xgboost!
          
          val fis = new java.io.FileInputStream(s"${modelPath}/pipeline_invoke.xgboost")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(modelPath, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }          
        
      new PMMLEvaluationCommand(modelName, 
                                modelTag, 
                                modelType, 
                                modelRuntime, 
                                modelChip, 
                                modelEvaluator, 
                                inputs, 
                                fallbackString, 
                                timeoutMillis, // 100 
                                concurrencyPoolSizeNumThreads, // 20 
                                rejectionThresholdNumRejections // 10
                                ).execute()
  }    

 
  // curl -i -X POST -v -H "Transfer-Encoding: chunked" \
  //  http://[host]:[port]/api/v1/model/invoke/jvm/spark/[model_name]/[model_tag]
  @RequestMapping(path=Array("/blah/blah/blah/api/v1/model/invoke/jvm/{modelChip}/spark/{modelName}/{modelTag}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
    def invokeSpark(@PathVariable("modelChip") modelChip: String,
                     @PathVariable("modelName") modelName: String,
                     @PathVariable("modelTag") modelTag: String,
                     @RequestBody inputJson: String): String = {
      
      val parsedInputOption = JSON.parseFull(inputJson)
      val inputs: Map[String, Any] = parsedInputOption match {
        case Some(parsedInput) => parsedInput.asInstanceOf[Map[String, Any]]
        case None => Map[String, Any]() 
      }

      val modelRuntime = "jvm"
      val modelType = "spark"
      
      val modelPath =  System.getenv("PIPELINE_RESOURCE_PATH")
      val modelEvaluatorOption = pmmlRegistry.get(modelPath)

      val modelEvaluator = modelEvaluatorOption match {
        case None => {     
          val fis = new java.io.FileInputStream(s"${modelPath}/pipeline_invoke.spark")
          val transformedSource = ImportFilter.apply(new InputSource(fis))
  
          val pmml = JAXBUtil.unmarshalPMML(transformedSource)
  
          val predicateOptimizer = new PredicateOptimizer()
          predicateOptimizer.applyTo(pmml)
  
          val predicateInterner = new PredicateInterner()
          predicateInterner.applyTo(pmml)
  
          val modelEvaluatorFactory = ModelEvaluatorFactory.newInstance()
  
          val modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml)
  
          // Cache modelEvaluator
          pmmlRegistry.put(modelPath, modelEvaluator)
          
          modelEvaluator
        }
        case Some(modelEvaluator) => modelEvaluator
      }                 
      
      new PMMLEvaluationCommand(modelName, 
                                modelTag,
                                modelType, 
                                modelRuntime, 
                                modelChip, 
                                modelEvaluator, 
                                inputs, 
                                fallbackString, 
                                timeoutMillis, // 100 
                                concurrencyPoolSizeNumThreads, // 20 
                                rejectionThresholdNumRejections // 10

          ).execute()
  }
  
 @RequestMapping(path=Array("/api/v1/model/invoke/redis/keyvalue/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def invokeKeyValue(@PathVariable("namespace") namespace: String,
                      @PathVariable("collection") collection: String,
                      @PathVariable("version") version: String,
                      @PathVariable("userId") userId: String, 
                      @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemPredictionCommand("redis", "redis_keyvalue_useritem", namespace, version, -1.0d, 25, 5, 10, userId, itemId)           
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  // Note that "keyvalue-batch" is a one-off.  Fix this.
  @RequestMapping(path=Array("/api/v1/model/invoke/redis/keyvalue-batch/{namespace}/{collection}/{version}/{userId}/{itemId}"),
                  produces=Array("application/json; charset=UTF-8"))
  def batchInvokeKeyValue(@PathVariable("namespace") namespace: String,
                           @PathVariable("collection") collection: String,
                           @PathVariable("version") version: String,
                           @PathVariable("userId") userId: String,
                           @PathVariable("itemId") itemId: String): String = {
    try {
      val result = new UserItemBatchPredictionCollapser("redis", "redis_keyvalue_useritem_batch", namespace, version, -1.0d, 25, 5, 10, userId, itemId)
        .execute()

      s"""{"result":${result}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }
  
  @RequestMapping(path=Array("/api/v1/model/invoke/redis/keyvalue/{namespace}/{collection}/{version}/{userId}/{startIdx}/{endIdx}"), 
                  produces=Array("application/json; charset=UTF-8"))
  def recommendations(@PathVariable("namespace") namespace: String,
                      @PathVariable("collection") collection: String,
                      @PathVariable("version") version: String,
                      @PathVariable("userId") userId: String, 
                      @PathVariable("startIdx") startIdx: Int, 
                      @PathVariable("endIdx") endIdx: Int): String = {
    try{
      
      // TODO:  try (Jedis jedis = pool.getResource()) { }; pool.destroy();

      val results = new RecommendationsCommand("redis", "redis_keyvalue_recommendations", jedisPool.getResource, namespace, version, userId, startIdx, endIdx)
       .execute()
      s"""{"outputs":${results.mkString(",")}}"""
    } catch {
       case e: Throwable => {
         throw e
       }
    }
  }

  @RequestMapping(path=Array("/api/v1/model/invoke/redis/keyvalue/{namespace}/{collection}/{version}/{itemId}/{startIdx}/{endIdx}"),
                  produces=Array("application/json; charset=UTF-8"))
  def similars(@PathVariable("namespace") namespace: String,
               @PathVariable("collection") collection: String,
               @PathVariable("version") version: String,
               @PathVariable("itemId") itemId: String, 
               @PathVariable("startIdx") startIdx: Int, 
               @PathVariable("endIdx") endIdx: Int): String = {
    try {
       val results = new ItemSimilarsCommand("redis", "item_similars", jedisPool.getResource, namespace, version, itemId, startIdx, endIdx)
         .execute()
       s"""{"outputs":${results.mkString(",")}}"""
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
