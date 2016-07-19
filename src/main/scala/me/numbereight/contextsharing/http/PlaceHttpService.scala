package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.foursquare.LatLong
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.Response
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Try

trait PlaceHttpService extends BaseHttpService {

  val routes = getPlace

  def getPlace: Route = get {
    pathPrefix(ApiVersion) {
      path("places") {
        parameters('ll, 'profile_id.?.as[Option[Long]]) { (latLong, profileId) => sprayCtx =>
          Try {
            val latLon = LatLong(latLong)
            val req = GetPlace(sprayCtx, latLon, profileId)
            placeActor.tell(req, ActorRef.noSender)
          }.recover {
            case t: Throwable =>
              sendResponse(sprayCtx, StatusCodes.BadRequest, Response(s"${t.getMessage}"))
          }
        }
      }
    }
  }

  protected def placeActor: ActorRef
}

object PlaceHttpService {
  def apply(system: ActorSystem, placeActorRef: ActorRef): PlaceHttpService = {
    new PlaceHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def placeActor: ActorRef = placeActorRef
    }
  }
}

