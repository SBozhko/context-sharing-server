package me.numbereight.contextsharing.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import me.numbereight.contextsharing.actor.ContextStorageActor.GetLastContextData
import me.numbereight.contextsharing.actor.ContextStorageActor.LastContextData
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import spray.routing.Route

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

trait RecommendationsHttpService extends BaseHttpService {

  val routes = getRecommendations

  def getRecommendations: Route = get {
    pathPrefix(ApiVersion) {
      path("recommendations" / LongNumber) { profileId => sprayCtx =>
        implicit val timeout = Timeout(5, TimeUnit.SECONDS)
        val future = contextHistoryActor.ask(GetLastContextData(profileId))

        future.onComplete {
          case Success(contextData: LastContextData) =>
            recommendationsActor.tell(GetItems(sprayCtx, profileId, contextData.result), ActorRef.noSender)
          case Failure(t) =>
            log.error(t, s"Unable to get context data for user with profile id: $profileId")
            recommendationsActor.tell(GetItems(sprayCtx, profileId, None), ActorRef.noSender)
        }
      }
    }
  }

  protected def recommendationsActor: ActorRef

  protected def contextHistoryActor: ActorRef
}

object RecommendationsHttpService {
  def apply(system: ActorSystem, recommendationsActorRef: ActorRef, contextHistoryActorRef: ActorRef): RecommendationsHttpService = {
    new RecommendationsHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def recommendationsActor: ActorRef = recommendationsActorRef

      override protected def contextHistoryActor: ActorRef = contextHistoryActorRef
    }
  }
}
