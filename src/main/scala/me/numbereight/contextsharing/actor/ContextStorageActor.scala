package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.DynamoDbClient
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitContextActorRequest
import spray.http.StatusCodes


class ContextStorageActor(dynamoClient: DynamoDbClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SubmitContextActorRequest =>
      // TODO: store in dynamo db
      sendResponse(msg.sprayCtx, StatusCodes.OK, Response("Successfully stored"))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object ContextStorageActor {

  val Name = "contextStorageActor"

  def props(dynamoClient: DynamoDbClient): Props = {
    Props.create(classOf[UserStatsActor], dynamoClient)
  }

}

