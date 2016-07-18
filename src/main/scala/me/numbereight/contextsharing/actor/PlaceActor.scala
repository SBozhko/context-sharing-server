package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.foursquare.FoursquareClient
import me.numbereight.contextsharing.model.ContextNames.Place
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.PlaceResponse
import spray.http.StatusCodes

class PlaceActor(foursquareClient: FoursquareClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
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
      sendResponse(msg.sprayCtx, StatusCodes.OK, PlaceResponse(minDistancePlace))
    case something: Any =>
      handleUnknownMsg(something)
  }
}


object PlaceActor {

  val Name = "placeActor"

  def props(foursquareClient: FoursquareClient): Props = {
    Props.create(classOf[PlaceActor], foursquareClient)
  }

}

