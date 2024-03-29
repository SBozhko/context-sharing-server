package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.IsAliveServiceActor.IsAlive
import me.numbereight.contextsharing.db.PostgresConnector
import me.numbereight.contextsharing.model.IsAliveResponse
import me.numbereight.contextsharing.model.Response
import spray.http.StatusCodes
import spray.routing.RequestContext

import scala.util.Failure
import scala.util.Success
import scala.util.Try

class IsAliveServiceActor(dbPoolName: String) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: IsAlive =>
      if (msg.checkDatabase) {
        Try(PostgresConnector.isAlive(dbPoolName)) match {
          case Success(success) =>
            if (success) {
              sendResponse(msg.ctx, StatusCodes.OK, IsAliveResponse(apiIsAlive = success, Some(true)))
            } else {
              sendResponse(msg.ctx, StatusCodes.InternalServerError, IsAliveResponse(apiIsAlive = true, Some(false)))
            }
          case Failure(exception) =>
            sendResponse(msg.ctx, StatusCodes.InternalServerError, Response(exception.getMessage))
        }
      } else {
        val response = IsAliveResponse(apiIsAlive = true)
        log.debug("IsAlive check was successful")
        sendResponse(msg.ctx, StatusCodes.OK, response)
      }
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object IsAliveServiceActor {

  val Name = "isAliveServiceActor"

  def props(dbPoolName: String): Props = {
    Props.create(classOf[IsAliveServiceActor], dbPoolName)
  }

  case class IsAlive(ctx: RequestContext, checkDatabase: Boolean)

}

