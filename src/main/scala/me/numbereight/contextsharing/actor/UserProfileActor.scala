package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.PostgresUserProfileClient
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitUserProfile
import me.numbereight.contextsharing.model.UserProfileResponse
import spray.http.StatusCodes


class UserProfileActor(client: PostgresUserProfileClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SubmitUserProfile =>
      client.saveUserProfile(msg.request) match {
        case Some(userId) =>
          log.debug(s"Stored user profile: ${msg.request}")
          sendResponse(msg.sprayCtx, StatusCodes.OK, UserProfileResponse(userId))
        case None =>
          log.debug(s"Unable to store user data: ${msg.request}")
          sendResponse(msg.sprayCtx, StatusCodes.InternalServerError, Response("Unable to store user data. Repeat the request"))
      }
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object UserProfileActor {

  val Name = "userProfileActor"

  val UserStatsMap = ContextNames.SampleUserStats.map(item => item.ctxGroup -> item.values).toMap

  def props(client: PostgresUserProfileClient): Props = {
    Props.create(classOf[UserProfileActor], client)
  }

}

