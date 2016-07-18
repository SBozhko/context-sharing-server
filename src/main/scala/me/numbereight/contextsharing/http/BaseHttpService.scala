package me.numbereight.contextsharing.http

import akka.actor.ActorSystem
import akka.event.Logging
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import spray.http.ContentTypes
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.StatusCode
import spray.routing.HttpService
import spray.routing.RequestContext

trait BaseHttpService extends HttpService {

  implicit val formats = DefaultFormats

  val log = Logging.getLogger(actorSystem, this)
  val ApiVersion = "v1"

  protected def actorSystem: ActorSystem

  def sendResponse(ctx: RequestContext, statusCode: StatusCode, response: AnyRef = None): Unit = {
    ctx.complete(jsonResponse(statusCode, response))
  }

  def jsonResponse(statusCode: StatusCode, response: AnyRef): HttpResponse = {
    HttpResponse(statusCode, HttpEntity(ContentTypes.`application/json`, Serialization.write(response)))
  }

}