package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.actor.IsAliveServiceActor.IsAlive
import me.numbereight.contextsharing.model.VersionResponse
import org.json4s.jackson.Serialization
import spray.http.ContentTypes
import spray.http.HttpEntity
import spray.http.HttpResponse
import spray.http.StatusCodes
import spray.routing.Route

trait ApplicationInfoHttpService extends BaseHttpService {

  val routes = isAliveCheck ~ getVersion

  def isAliveCheck: Route = get {
    pathPrefix(ApiVersion) {
      path("isAlive") {
        parameters('database.?(false)) { database => ctx =>
          isAliveActor.tell(IsAlive(ctx, database), ActorRef.noSender)
        }
      }
    }
  }

  def getVersion: Route = get {
    pathPrefix(ApiVersion) {
      path("version") { ctx =>
        ctx.complete(
          HttpResponse(
            StatusCodes.OK,
            HttpEntity(ContentTypes.`application/json`, Serialization.write(VersionResponse.load))))
      }
    }
  }

  protected def isAliveActor: ActorRef
}

object ApplicationInfoHttpService {
  def apply(system: ActorSystem, alivenessActor: ActorRef): ApplicationInfoHttpService = {
    new ApplicationInfoHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def isAliveActor: ActorRef = alivenessActor
    }
  }
}
