package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import spray.routing.Route

trait RecommendationsHttpService extends BaseHttpService {

  val routes = getRecommendations

  def getRecommendations: Route = get {
    pathPrefix(ApiVersion) {
      path("recommendations" / LongNumber) { profileId => sprayCtx =>
        recommendationsActor.tell(GetItems(sprayCtx, profileId), ActorRef.noSender)
      }
    }
  }

  protected def recommendationsActor: ActorRef
}

object RecommendationsHttpService {
  def apply(system: ActorSystem, recommendationsActorRef: ActorRef): RecommendationsHttpService = {
    new RecommendationsHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def recommendationsActor: ActorRef = recommendationsActorRef
    }
  }
}
