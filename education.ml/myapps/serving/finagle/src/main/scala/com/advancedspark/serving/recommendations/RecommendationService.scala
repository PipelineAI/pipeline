package com.advancedspark.serving.recommendations

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Http, Request, Response, Status}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{DefaultHttpResponse, HttpVersion, HttpResponseStatus, HttpRequest, HttpResponse}
import java.net.{SocketAddress, InetSocketAddress}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService

import better.files.ThreadBackedFileMonitor
import better.files.File
import java.nio.file.WatchEvent

import scala.util.parsing.json._

object RecommendationServer extends App {
  new ThreadBackedFileMonitor(File("/root/pipeline/datasets/serving/live-recommendations/json"), recursive = true) {
    override def onCreate(file: File) = {
      if (file.name.contains("itemId=")) {
      println(s"New file:  $file")

      if (!file.isDirectory()) {     
        val jsonStr = new String(file.byteArray)
        println(s"Contents:  $jsonStr")

        val result = JSON.parseFull(jsonStr)
        result match {
          case Some(map: Map[String, Any]) => println(map)
          case None => println("Parsing failed")
          case other => println("Unknown data structure: " + other)
        }
      }
    }
    }
    //override def onModify(file: File) = println(s"Modified file:  $file")
    //override def onDelete(file: File) = println(s"Deleted file:  $file")
    override def onUnknownEvent(event: WatchEvent[_]) = println(event)
    override def onException(exception: Throwable) = println(exception)
  }.start()

  def elasticSearchRecommendationService(userId: Int, itemId: Int) = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response(request.version, Status.Ok)   
      val prediction = new ElasticSearchPredictionCommand(userId, itemId).execute()
      val items = Array(s"""userId:${userId}, itemId:${itemId}, confidence:${prediction}""")
      response.setContentString(items.mkString(","))
      
      Future.value(response)
    }
  }

  def watchedJsonRecommendationService(userId: Int, itemId: Int) = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response(request.version, Status.Ok)
      val prediction = new WatchedJsonPredictionCommand(userId, itemId).execute()
      val items = Array(s"""userId:${userId}, itemId:${itemId}, confidence:${prediction}""")
      response.setContentString(items.mkString(","))

      Future.value(response)
    }
  }

  val blackHole = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = Future.never
  }

  val router = RoutingService.byPathObject[Request] {
    case Root / "predict" / Integer(userId) / Integer(itemId) => elasticSearchRecommendationService(userId, itemId)
    case Root / "predict-json" / Integer(userId) / Integer(itemId) => watchedJsonRecommendationService(userId, itemId)
    case _ => blackHole
  }

  val address: SocketAddress = new InetSocketAddress("0.0.0.0", 5080)

  val server: Server = ServerBuilder()
    .codec(Http.get())
    .bindTo(address)
    .name("RecommendationService")
    .build(router)
}
