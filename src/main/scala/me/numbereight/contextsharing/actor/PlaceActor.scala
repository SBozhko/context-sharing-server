package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.foursquare.FoursquareClient
import me.numbereight.contextsharing.model.GetPlace
import me.numbereight.contextsharing.model.PlaceResponse
import spray.http.StatusCodes

class PlaceActor(foursquareClient: FoursquareClient) extends BaseHttpServiceActor {
  override def receive: Actor.Receive = {
    case msg: GetPlace =>
//      val res = Place.AllMeaningful.map { place =>
//        place -> foursquareClient.performLocationCategoryQuery(msg.latLon, place)
//      }
//      Future.sequence(res).map { item =>
//
//      }

//    client.saveContextData(msg.request)
    sendResponse(msg.sprayCtx, StatusCodes.OK, PlaceResponse("Home"))
    //log.debug(s"Stored contextData: ${msg.request}")
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

