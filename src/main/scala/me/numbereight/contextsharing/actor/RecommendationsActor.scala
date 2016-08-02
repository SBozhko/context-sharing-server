package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import me.numbereight.contextsharing.model.RecommendationMusicItem
import me.numbereight.contextsharing.model.RecommendationVideoItem
import me.numbereight.contextsharing.model.RecommendationsResponse
import spray.http.StatusCodes
import spray.routing.RequestContext

class RecommendationsActor() extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetItems =>
      log.debug(s"Get recommendations request for profile id: ${msg.profileId}")

      val response = RecommendationsResponse(
        List(
          RecommendationMusicItem(1, "Batracios - El Foforro", "https://soundcloud.com/user-277220310/el-foforro"),
          RecommendationMusicItem(2, "Batracios - Tal Vez", "https://soundcloud.com/user-277220310/tal-vez"),
          RecommendationMusicItem(3, "Jesus Most Difficult Teaching Side A - WalkWithGod", "https://soundcloud.com/user-72290942/jesus-most-difficult-teaching-side-a"),
          RecommendationMusicItem(4, "Batracios - Ska Oi", "https://soundcloud.com/user-277220310/ska-oi"),
          RecommendationMusicItem(5, "Batracios - Un Camino", "https://soundcloud.com/user-277220310/un-camino")),
        List(
          RecommendationVideoItem(1, "Team Refugee: Rio Olympic hopefuls running for a better life - BBC News", "https://www.youtube.com/watch?v=lKrYRL6OiR4"),
          RecommendationVideoItem(2, "North Korea: Defectors' stories - BBC News", "https://www.youtube.com/watch?v=XUgUMfmsw3s"),
          RecommendationVideoItem(3, "Why are people in oil-rich Venezuela going hungry? BBC News", "https://www.youtube.com/watch?v=Jga48jFr004"),
          RecommendationVideoItem(4, "Cameron warns leaving EU is a 'step into the dark' - BBC News", "https://www.youtube.com/watch?v=VTLzRHrGHds"),
          RecommendationVideoItem(5, "President Obama's message of hope - BBC News", "https://www.youtube.com/watch?v=VbW_NjCEjak")
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

