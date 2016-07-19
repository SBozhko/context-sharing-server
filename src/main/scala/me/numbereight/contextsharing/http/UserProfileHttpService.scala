package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitUserInfoMessage
import me.numbereight.contextsharing.model.SubmitUserInfoRequest
import me.numbereight.contextsharing.model.SubmitUserProfile
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait UserProfileHttpService extends BaseHttpService {

  val routes = postUserInfo()

  def postUserInfo(): Route = post {
    pathPrefix(ApiVersion) {
      path("users") { sprayCtx =>
        val req = Serialization.read[SubmitUserInfoRequest](sprayCtx.request.entity.asString)
        if (!req.timezone.startsWith("UTC")) {
          sendResponse(sprayCtx, StatusCodes.BadRequest, Response("Wrong timezone format. Examples: UTC+0000, UTC-0500, UTC+0650"))
        }
        val timezoneStr = req.timezone.replaceFirst("UTC", "")
        Try(timezoneStr.toDouble / 100) match {
          case Success(timezone) =>
            val userInfoMessage = SubmitUserInfoMessage(req.userId, req.vendorId, req.advertisingId, (timezone * 60).toInt)
            val message = SubmitUserProfile(sprayCtx, userInfoMessage)
            userProfileActor.tell(message, ActorRef.noSender)
          case Failure(_) =>
            sendResponse(sprayCtx, StatusCodes.BadRequest, Response("Wrong timezone format. Examples: UTC+0000, UTC-0500, UTC+0650"))
        }

      }
    }
  }

  protected def userProfileActor: ActorRef
}

object UserProfileHttpService {
  def apply(system: ActorSystem, userProfileActorRef: ActorRef): UserProfileHttpService = {
    new UserProfileHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def userProfileActor: ActorRef = userProfileActorRef
    }
  }
}
