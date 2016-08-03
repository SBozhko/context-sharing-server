package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import me.numbereight.contextsharing.model.MusicItem
import me.numbereight.contextsharing.model.OrderItem
import me.numbereight.contextsharing.model.RecommendationsResponse
import me.numbereight.contextsharing.model.VideoItem
import spray.http.StatusCodes
import spray.routing.RequestContext

class RecommendationsActor() extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetItems =>
      log.debug(s"Get recommendations request for profile id: ${msg.profileId}")

      val response = RecommendationsResponse(
        List(
          OrderItem("musicItems", 1),
          OrderItem("musicItems", 4),
          OrderItem("videoItems", 1),
          OrderItem("musicItems", 2),
          OrderItem("musicItems", 5),
          OrderItem("videoItems", 3),
          OrderItem("musicItems", 3),
          OrderItem("videoItems", 5),
          OrderItem("videoItems", 4),
          OrderItem("videoItems", 3)
        ),
        List(
          MusicItem(1, "Batracios - El Foforro", "https://soundcloud.com/user-277220310/el-foforro"),
          MusicItem(2, "Batracios - Tal Vez", "https://soundcloud.com/user-277220310/tal-vez"),
          MusicItem(3, "Jesus Most Difficult Teaching Side A - WalkWithGod", "https://soundcloud.com/user-72290942/jesus-most-difficult-teaching-side-a"),
          MusicItem(4, "Batracios - Ska Oi", "https://soundcloud.com/user-277220310/ska-oi"),
          MusicItem(5, "Batracios - Un Camino", "https://soundcloud.com/user-277220310/un-camino")),
        List(
          VideoItem(1, "Team Refugee: Rio Olympic hopefuls running for a better life - BBC News", "https://www.youtube.com/watch?v=lKrYRL6OiR4"),
          VideoItem(2, "North Korea: Defectors' stories - BBC News", "https://www.youtube.com/watch?v=XUgUMfmsw3s"),
          VideoItem(3, "Why are people in oil-rich Venezuela going hungry? BBC News", "https://www.youtube.com/watch?v=Jga48jFr004"),
          VideoItem(4, "Cameron warns leaving EU is a 'step into the dark' - BBC News", "https://www.youtube.com/watch?v=VTLzRHrGHds"),
          VideoItem(5, "President Obama's message of hope - BBC News", "https://www.youtube.com/watch?v=VbW_NjCEjak")
        ))

      sendResponse(msg.sprayCtx, StatusCodes.OK, response)
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object RecommendationsActor {

  val Name = "recommendationsActor"

  def props(): Props = {
    Props.create(classOf[RecommendationsActor])
  }

  case class GetItems(sprayCtx: RequestContext, profileId: Long)

}

