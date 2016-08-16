package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.ContextStorageActor.GetLastContextData
import me.numbereight.contextsharing.actor.ContextStorageActor.LastContextData
import me.numbereight.contextsharing.actor.ContextStorageActor.SubmitContextActorRequest
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.model.ContextDataPair
import me.numbereight.contextsharing.model.SubmitContextResponse
import spray.http.StatusCodes
import spray.routing.RequestContext


class ContextStorageActor(client: PostgresContextHistoryClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SubmitContextActorRequest =>
      client.saveContextData(msg.profileId, msg.contextData)
      val responseList = msg.contextData.map(item => ContextDataPair(item.ctxGroup, item.ctxName))
      sendResponse(msg.sprayCtx, StatusCodes.OK, SubmitContextResponse(responseList))
      log.debug(s"Stored contextData: ${msg.contextData} for profileId ${msg.profileId}")
    case msg: GetLastContextData =>
      sender.tell(LastContextData(client.getLastContextData(msg.profileId)), context.self)
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object ContextStorageActor {

  val Name = "contextStorageActor"

  def props(client: PostgresContextHistoryClient): Props = {
    Props.create(classOf[ContextStorageActor], client)
  }

  case class GetLastContextData(profileId: Long)

  case class LastContextData(result: Option[ContextDataPair])

  case class SubmitContextActorRequest(sprayCtx: RequestContext, profileId: Long, contextData: List[ContextDataPair])

}

