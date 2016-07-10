package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.ContextGroups
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsRequest
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Try

trait UserStatsHttpService extends BaseHttpService {

  val routes = getStats

  def getStats: Route = get {
    pathPrefix(ApiVersion) {
      path("contexts" / Segment / Segment) { (userId, vendorId) =>
        parameters('ctx.?) { contexts => sprayCtx =>
          Try {
            val ctxGroups = contexts match {
              case Some(ctxAsString) => ctxAsString.split(",").toList
              case None => ContextGroups.All.toList
            }
            ctxGroups.forall(item => ContextGroups.valid(item)) match {
              case true =>
                val req = GetUserStatsRequest(userId, vendorId, ctxGroups)
                userStatsActor.tell(GetUserStatsActorRequest(sprayCtx, req), ActorRef.noSender)
              case false =>
                val errMsg = s"Context types should be comma separated, like: ${ContextGroups.All.mkString(",")}"
                sprayCtx.complete(StatusCodes.BadRequest, errMsg)
            }
          }.recover {
            case t: Throwable => sprayCtx.complete(StatusCodes.BadRequest, s"${t.getMessage}")
          }
        }
      }
    }
  }

  protected def userStatsActor: ActorRef
}

object UserStatsHttpService {
  def apply(system: ActorSystem, statsActor: ActorRef): UserStatsHttpService = {
    new UserStatsHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def userStatsActor: ActorRef = statsActor
    }
  }
}