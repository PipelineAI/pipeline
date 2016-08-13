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

//  @BeanProperty
//  @Value("${prediction.tree.pmml:'data/census/census.pmml'}")
//  var treePmml = ""

  @RequestMapping(path=Array("/evaluate/{pmmlName}"),
                  method=Array(RequestMethod.POST),
                  produces=Array("application/json; charset=UTF-8"))
  def evaluatePmml(@PathVariable("pmmlName") pmmlName: String, @RequestBody inputs: String): String = {
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
