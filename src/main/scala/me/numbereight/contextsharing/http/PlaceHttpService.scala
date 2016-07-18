package me.numbereight.contextsharing.http

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.foursquare.LatLon
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitUserInfoRequest
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Failure
import scala.util.Success
import scala.util.Try

trait PlaceHttpService extends BaseHttpService {

  val routes = getPlace ~ postUserInfo()

  def getPlace: Route = get {
    pathPrefix(ApiVersion) {
      path("places") {
        parameters('ll, 'vendor_id.?) { (latLong, vendorId) => sprayCtx =>
          Try {
            val latLon = LatLon(latLong)
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


  def postUserInfo(): Route = post {
    pathPrefix(ApiVersion) {
      path("users") { sprayCtx =>
        val req = Serialization.read[SubmitUserInfoRequest](sprayCtx.request.entity.asString)
        if (!req.timezone.startsWith("UTC")) {
          sendResponse(sprayCtx, StatusCodes.BadRequest, Response("Wrong timezone format. Examples: UTC+0, UTC-5, UTC+6.5'"))
        }
        val timezoneStr = req.timezone.replaceFirst("UTC", "")
        Try(timezoneStr.toDouble) match {
          case Success(timezone) =>
            println(timezone)
            println(timezone * 3600000)
            sendResponse(sprayCtx, StatusCodes.OK, Response(s"Timezone ${req.timezone} successfully stored"))
          case Failure(_) =>
            sendResponse(sprayCtx, StatusCodes.BadRequest, Response("Wrong timezone format. Examples: UTC+0, UTC-5, UTC+6.5'"))
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

