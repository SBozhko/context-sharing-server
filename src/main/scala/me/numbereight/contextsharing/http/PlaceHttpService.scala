package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.actor.PlaceActor.SetPlace
import me.numbereight.contextsharing.foursquare.LatLong
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitPlaceRequest
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Try

trait PlaceHttpService extends BaseHttpService {

  val routes = getPlace ~ setPlace()

  def getPlace: Route = get {
    pathPrefix(ApiVersion) {
      path("places") {
        parameters('ll, 'vendor_id.?) { (latLong, vendorId) => sprayCtx =>
          Try {
            val latLon = LatLong(latLong)
            val req = GetPlace(sprayCtx, latLon, vendorId)
            placeActor.tell(req, ActorRef.noSender)
          }.recover {
            case t: Throwable =>
              sendResponse(sprayCtx, StatusCodes.BadRequest, Response(s"${t.getMessage}"))
          }
        }
      }
    }
  }

  def setPlace(): Route = post {
    pathPrefix(ApiVersion) {
      path("places") { sprayCtx =>
        Try {
          val req = Serialization.read[SubmitPlaceRequest](sprayCtx.request.entity.asString)
          val latLong = LatLong(req.lat, req.long)
          val setPlaceMsg = SetPlace(sprayCtx, req.placeName, latLong, req.vendorId)
          placeActor.tell(setPlaceMsg, ActorRef.noSender)
        }.recover {
          case t: Throwable =>
            sendResponse(sprayCtx, StatusCodes.BadRequest, Response(s"${t.getMessage}"))
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

