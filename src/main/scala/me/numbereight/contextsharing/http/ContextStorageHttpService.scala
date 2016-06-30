package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.SubmitContextActorRequest
import me.numbereight.contextsharing.model.SubmitContextRequest
import org.json4s.jackson.Serialization
import spray.routing.Route

trait ContextStorageHttpService extends BaseHttpService {

  val routes = storeContext()

  def storeContext(): Route = post {
    pathPrefix(ApiVersion) {
      path("contexts") { sprayCtx =>
        val req = Serialization.read[SubmitContextRequest](sprayCtx.request.entity.asString)
        ctxStorageActor.tell(SubmitContextActorRequest(sprayCtx, req), ActorRef.noSender)
      }
    }
  }

  protected def ctxStorageActor: ActorRef
}

object ContextStorageHttpService {
  def apply(system: ActorSystem, storageActor: ActorRef): ContextStorageHttpService = {
    new ContextStorageHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def ctxStorageActor: ActorRef = storageActor
    }
  }
}