package me.numbereight.contextsharing.actor


import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.DynamoDbClient
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.CtxStats
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsResponse
import spray.http.StatusCodes


class UserStatsActor(dynamoClient: DynamoDbClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetUserStatsActorRequest =>
      // TODO: go to DB and check stats
      val filteredValues = msg.request.ctxGroups.map(item => CtxStats(item, UserStatsActor.UserStatsMap(item)))
      sendResponse(msg.sprayCtx, StatusCodes.OK, GetUserStatsResponse(filteredValues))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object UserStatsActor {

  val Name = "contextStorageActor"

  val UserStatsMap = ContextNames.SampleUserStats.map(item => item.ctxGroup -> item.values).toMap

  def props(dynamoClient: DynamoDbClient): Props = {
    Props.create(classOf[UserStatsActor], dynamoClient)
  }

}

