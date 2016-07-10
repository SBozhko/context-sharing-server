package me.numbereight.contextsharing.actor


import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsResponse
import spray.http.StatusCodes


class UserStatsActor(client: PostgresContextHistoryClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetUserStatsActorRequest =>
      val filteredValues = client.getStats(msg.request)
      sendResponse(msg.sprayCtx, StatusCodes.OK, GetUserStatsResponse(filteredValues))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object UserStatsActor {

  val Name = "contextStorageActor"

  val UserStatsMap = ContextNames.SampleUserStats.map(item => item.ctxGroup -> item.values).toMap

  def props(client: PostgresContextHistoryClient): Props = {
    Props.create(classOf[UserStatsActor], client)
  }

}

