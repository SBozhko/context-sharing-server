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
          MusicItem(1, "El Foforro", "Batracios", "https://soundcloud.com/user-277220310/el-foforro", 217306, "http://media-assets-04.thedrum.com/cache/images/thedrum-prod/news-tmp-116055-soundcloud-logo--default--300.png"),
          MusicItem(2, "Tal Vez", "Batracios", "https://soundcloud.com/user-277220310/tal-vez", 268040, "https://i1.sndcdn.com/artworks-000174523443-49f0xg-t300x300.jpg"),
          MusicItem(3, "A Murphy's", "Batracios", "https://soundcloud.com/user-277220310/a-murphys", 241785, "https://i1.sndcdn.com/artworks-000174523430-hhm2b0-t300x300.jpg"),
          MusicItem(4, "Blessed (Prod. By theSOURCE)", "theSOURCE", "https://soundcloud.com/thesource1107/blessed-prod-by-thesource", 327330, "http://media-assets-04.thedrum.com/cache/images/thedrum-prod/news-tmp-116055-soundcloud-logo--default--300.png"),
          MusicItem(5, "TTFFWW - Namex", "Namex & Answer", "https://soundcloud.com/moises-romero-nexus/ttffww-namex", 233608, "http://media-assets-04.thedrum.com/cache/images/thedrum-prod/news-tmp-116055-soundcloud-logo--default--300.png")),
        List(
          VideoItem(1, "Team Refugee: Rio Olympic hopefuls running for a better life - BBC News", "https://www.youtube.com/watch?v=lKrYRL6OiR4", 215000, "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png"),
          VideoItem(2, "North Korea: Defectors' stories - BBC News", "https://www.youtube.com/watch?v=XUgUMfmsw3s", 296000, "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png"),
          VideoItem(3, "Why are people in oil-rich Venezuela going hungry? BBC News", "https://www.youtube.com/watch?v=Jga48jFr004", 218000, "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png"),
          VideoItem(4, "Cameron warns leaving EU is a 'step into the dark' - BBC News", "https://www.youtube.com/watch?v=VTLzRHrGHds", 3810000, "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png"),
          VideoItem(5, "President Obama's message of hope - BBC News", "https://www.youtube.com/watch?v=VbW_NjCEjak", 139000, "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png")
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

