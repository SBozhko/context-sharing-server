package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.PlaceActor.SetPlace
import me.numbereight.contextsharing.db.PostgresPlaceHistoryClient
import me.numbereight.contextsharing.foursquare.FoursquareClient
import me.numbereight.contextsharing.foursquare.LatLong
import me.numbereight.contextsharing.model.ContextNames.Place
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.PlaceEvent
import me.numbereight.contextsharing.model.PlaceResponse
import me.numbereight.contextsharing.model.Response
import spray.http.StatusCodes
import spray.routing.RequestContext

class PlaceActor(foursquareClient: FoursquareClient, pgClient: PostgresPlaceHistoryClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: SetPlace =>
      // TODO: Impl
      // TODO: update history
      sendResponse(msg.sprayCtx, StatusCodes.OK, Response(s"Place category updated to '${msg.placeName}'"))
    case msg: GetPlace =>
      val distances = Place.AllMeaningful.par.map { place =>
        place -> foursquareClient.performLocationCategoryQuery(msg.latLong, place)
      }.filter(_._2.isDefined)

      val minDistancePlace = if (distances.isEmpty) {
        log.warning(s"No place by location: ${msg.latLong}")
        Place.Other
      } else {
        distances.minBy(_._2)._1
      }

      if (msg.vendorId.isDefined) {
        pgClient.saveContextData(PlaceEvent(msg.latLong, msg.vendorId.get, minDistancePlace)) // TODO: add retry
      }
      sendResponse(msg.sprayCtx, StatusCodes.OK, PlaceResponse(minDistancePlace))
    case something: Any =>
      handleUnknownMsg(something)
  }
}


object PlaceActor {

  case class SetPlace(sprayCtx: RequestContext, placeName: String, latLong: LatLong)

  val Name = "placeActor"

  def props(foursquareClient: FoursquareClient, pgPlaceClient: PostgresPlaceHistoryClient): Props = {
    Props.create(classOf[PlaceActor], foursquareClient, pgPlaceClient)
  }

}

