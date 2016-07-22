package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.ContextGroups
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsRequest
import me.numbereight.contextsharing.model.StatsPeriod
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Try

trait UserStatsHttpService extends BaseHttpService {

  val routes = getStats

  def getStats: Route = get {
    pathPrefix(ApiVersion) {
      path("contexts" / LongNumber) { profileId =>
        parameters('ctx.?, 'period.?("day")) { (contexts, period) => sprayCtx =>
          Try {
            val ctxGroups = contexts match {
              case Some(ctxAsString) => ctxAsString.split(",").toList
              case None => ContextGroups.All.toList
            }

            if (!StatsPeriod.valid(period)) {
              throw new IllegalArgumentException("Illegal period name. Should be one of: day, week, month.")
            }

            ctxGroups.forall(item => ContextGroups.valid(item)) match {
              case true =>
                val req = GetUserStatsRequest(profileId, ctxGroups, period)
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