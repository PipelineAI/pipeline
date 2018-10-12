package ai.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaSetConverter

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import redis.clients.jedis.Jedis

class RecommendationsCommand(runtimeType: String,
                             commandName: String, 
                             jedis: Jedis, 
                             namespace: String, 
                             version: String, 
                             userId: String, 
                             startIdx: Int, 
                             endIdx: Int) 
  extends HystrixCommand[Seq[String]](HystrixCommandGroupKey.Factory.asKey(commandName)) {

  def run(): Seq[String] = {
    try{
      jedis.zrevrange(s"${namespace}:${version}:recommendations:${userId}", startIdx, endIdx).asScala.toSeq
    } catch { 
       case e: Throwable => {
         System.out.println(e) 
         throw e
       }
    }
  }

  override def getFallback(): Seq[String] = {
    System.out.println("Recommendations Source is Down!  Fallback!!")

    List("10001", "10002", "10003", "10004", "10005")
  }
}
