package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.ContextStorageActor.GetLastContextData
import me.numbereight.contextsharing.actor.ContextStorageActor.LastContextData
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.model.ContextData
import me.numbereight.contextsharing.model.ContextDataPair
import me.numbereight.contextsharing.model.SubmitContextActorRequest
import me.numbereight.contextsharing.model.SubmitContextResponse
import spray.http.StatusCodes


class ContextStorageActor(client: PostgresContextHistoryClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SubmitContextActorRequest =>
      client.saveContextData(msg.request)
      val responseList = msg.request.contextData.map(item => ContextDataPair(item.ctxGroup, item.ctxName))
      sendResponse(msg.sprayCtx, StatusCodes.OK, SubmitContextResponse(responseList))
      log.debug(s"Stored contextData: ${msg.request}")
    case msg: GetLastContextData =>
      sender.tell(LastContextData(client.getLastContextData(msg.profileId)), context.self)
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object ContextStorageActor {

  case class GetLastContextData(profileId: Long)
  case class LastContextData(result: Option[ContextData])

  val Name = "contextStorageActor"

  def props(client: PostgresContextHistoryClient): Props = {
    Props.create(classOf[ContextStorageActor], client)
  }

}

