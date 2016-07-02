package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.SubmitContextActorRequest
import me.numbereight.contextsharing.model.SubmitContextRequest
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

trait ContextStorageHttpService extends BaseHttpService {

  val routes = storeContext()

  def storeContext(): Route = post {
    pathPrefix(ApiVersion) {
      path("contexts") { sprayCtx =>
        val req = Serialization.read[SubmitContextRequest](sprayCtx.request.entity.asString)
        req.contextData.forall(item => ContextNames.valid(item.ctxGroup, item.ctxName)) match {
          case true => ctxStorageActor.tell(SubmitContextActorRequest(sprayCtx, req), ActorRef.noSender)
          case false =>
            sprayCtx.complete(StatusCodes.BadRequest, "Wrong ContextGroup(s) or ContextName(s)")
        }
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