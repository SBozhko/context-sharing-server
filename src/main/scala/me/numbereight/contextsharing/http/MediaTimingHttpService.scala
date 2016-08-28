package me.numbereight.contextsharing.http

import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import me.numbereight.contextsharing.model.Response
import me.numbereight.contextsharing.model.SubmitMediaTimingRequest
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

trait MediaTimingHttpService extends BaseHttpService {

  val routes = postTrackTiming()

  def postTrackTiming(): Route = post {
    pathPrefix(ApiVersion) {
      path("mediaTimings") { sprayCtx =>
        val req = Serialization.read[SubmitMediaTimingRequest](sprayCtx.request.entity.asString)
        if (req.`type` == "musicItems" || req.`type` == "videoItems") {
          log.debug(req.toString)
          sendResponse(sprayCtx, StatusCodes.OK, Response("Submitted successfully"))
        } else {
          sendResponse(sprayCtx, StatusCodes.BadRequest, Response("Wrong type. Should be musicItems or videoItems"))
        }
      }
    }
  }
}

object MediaTimingHttpService {
  def apply(system: ActorSystem): MediaTimingHttpService = {
    new MediaTimingHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system
    }
  }
}
