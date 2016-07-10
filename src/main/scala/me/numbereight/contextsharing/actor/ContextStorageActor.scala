package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitContextActorRequest
import spray.http.StatusCodes


class ContextStorageActor(client: PostgresContextHistoryClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SubmitContextActorRequest =>
      client.saveContextData(msg.request)
      sendResponse(msg.sprayCtx, StatusCodes.OK, Response("Successfully stored"))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object ContextStorageActor {

  val Name = "contextStorageActor"

  def props(client: PostgresContextHistoryClient): Props = {
    Props.create(classOf[ContextStorageActor], client)
  }

}

