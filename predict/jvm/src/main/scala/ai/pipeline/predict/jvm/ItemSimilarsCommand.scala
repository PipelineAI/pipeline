package ai.pipeline.predict.jvm

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.immutable.List

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey

import redis.clients.jedis.Jedis

class ItemSimilarsCommand(runtimeType: String, 
                          commandName: String, 
                          dynoClient: Jedis, 
                          namespace: String, 
                          version: String, 
                          itemId: String, 
                          startIdx: Int, 
                          endIdx: Int) 
  extends HystrixCommand[Seq[(String)]](HystrixCommandGroupKey.Factory.asKey(commandName)) {

  def run(): Seq[String] = {
    try{
      dynoClient.zrevrange(s"${namespace}:${version}:item-similars:${itemId}", startIdx, endIdx).asScala.toSeq
    } catch {
       case e: Throwable => {
         System.out.println(e)
         throw e
       }
    }
  }

  override def getFallback(): Seq[String] = {
    System.out.println("ItemSimilars Source is Down!  Fallback!!")

    List("10001", "10002", "10003", "10004", "10005")
  }
}
