package me.numbereight.contextsharing.actor

import akka.actor.ActorLogging
import me.numbereight.contextsharing.model.Response
import org.json4s.DefaultFormats
import org.json4s.MappingException
import org.json4s.ParserUtil
import org.json4s.jackson.Serialization
import spray.http.ContentTypes
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.StatusCode
import spray.http.StatusCodes
import spray.routing.ExceptionHandler
import spray.routing.HttpServiceActor
import spray.routing.MalformedQueryParamRejection
import spray.routing.MalformedRequestContentRejection
import spray.routing.MissingQueryParamRejection
import spray.routing.RejectionHandler
import spray.routing.RequestContext
import spray.util.LoggingContext

trait BaseHttpServiceActor extends HttpServiceActor with ActorLogging {

  implicit val formats = DefaultFormats

  implicit def exceptionHandler(implicit log: LoggingContext): ExceptionHandler = {
    ExceptionHandler {
      case e: Exception => ctx => {
        handleException(ctx, e)
      }
    }
  }

  implicit def rejectionHandler: RejectionHandler = RejectionHandler {
    case MalformedRequestContentRejection(msg, _) :: _ =>
      log.warning("Malformed json detected: {}", msg)
      complete(jsonResponse(StatusCodes.BadRequest, Response("Invalid JSON")))
    case MalformedQueryParamRejection(parameterName, msg, _) :: _ =>
      log.warning("Query parameter: {}; wrong value: {}", parameterName, msg)
      complete(jsonResponse(StatusCodes.BadRequest, Response(s"Invalid value for query parameter: $parameterName")))
    case MissingQueryParamRejection(parameterName) :: _ =>
      log.warning("Query parameter: {} is absent", parameterName)
      complete(jsonResponse(StatusCodes.BadRequest, Response(s"Missing query parameter: $parameterName")))
  }

  def handleException(ctx: RequestContext, exception: Exception) {
    exception match {
      case e@(_: MappingException | _: ParserUtil.ParseException) =>
        log.warning("Invalid json detected: {}", e)
        sendResponse(ctx, StatusCodes.BadRequest, Response("Invalid JSON", e.getMessage))
      case e: Exception =>
        log.error(e, "Unknown Exception was handled")
        sendResponse(ctx, StatusCodes.InternalServerError, Response("Unknown error", e.getMessage))
    }
  }

  def sendResponse(ctx: RequestContext, statusCode: StatusCode, response: AnyRef = None): Unit = {
    ctx.complete(jsonResponse(statusCode, response))
  }

  def jsonResponse(statusCode: StatusCode, response: AnyRef): HttpResponse = {
    HttpResponse(statusCode, HttpEntity(ContentTypes.`application/json`, Serialization.write(response)))
  }

  def handleUnknownMsg(something: Any) {
    log.warning("Unknown message was received: {}", something)
    unhandled(something)
  }
}
