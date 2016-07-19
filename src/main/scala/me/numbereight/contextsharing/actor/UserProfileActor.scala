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
      client.getUserProfileId(msg.request.advertisingId) match {
        case Some(profileId) =>
          sendResponse(msg.sprayCtx, StatusCodes.OK, UserProfileResponse(profileId)) // TODO: Update timezone, userId is needed in background
        case None =>
          client.saveUserProfile(msg.request) match {
            case Some(profileId) =>
              log.debug(s"Stored user profile: ${msg.request}")
              sendResponse(msg.sprayCtx, StatusCodes.OK, UserProfileResponse(profileId))
            case None =>
              log.warning(s"Unable to store user data: ${msg.request}")
              sendResponse(msg.sprayCtx, StatusCodes.InternalServerError, Response("Unable to store user data. Repeat the request"))
          }
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

