package com.advancedspark.serving.recommendations

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Http, Request, Response, Status}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{DefaultHttpResponse, HttpVersion, HttpResponseStatus, HttpRequest, HttpResponse}
import java.net.{SocketAddress, InetSocketAddress}
import com.twitter.finagle.builder.{Server, ServerBuilder}

object RecommendationServer extends App {
  val recommendationService = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = request.uri match {
        case _ => {
          val userId = request.getIntParam("userId", 12663)
          val itemId = request.getIntParam("itemId", 7)
          val prediction = new PredictionCommand(userId, itemId).execute()
          val recommendResponse = Response(request.version, Status.Ok)
          val items = Array(s"""userId:${userId}, itemId:${itemId}, confidence:${prediction}""")
          recommendResponse.setContentString(items.mkString(","))
          recommendResponse
        }
        //case _ => Response(request.version, Status.NotFound)
      }
      Future.value(response)
    }
  }

  val address: SocketAddress = new InetSocketAddress("0.0.0.0", 5080)

  val server: Server = ServerBuilder()
    .codec(Http.get())
    .bindTo(address)
    .name("RecommendationService")
    .build(recommendationService)
}
