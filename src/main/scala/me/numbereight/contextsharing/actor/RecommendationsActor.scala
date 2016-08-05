package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import me.numbereight.contextsharing.model.ContextData
import me.numbereight.contextsharing.model.ContextNames.Situation
import me.numbereight.contextsharing.model.MusicItem
import me.numbereight.contextsharing.model.OrderItem
import me.numbereight.contextsharing.model.RecommendationsResponse
import me.numbereight.contextsharing.model.VideoItem
import me.numbereight.contextsharing.soundcloud.SoundCloudClient
import spray.http.StatusCodes
import spray.routing.RequestContext

import scala.util.Random

class RecommendationsActor(soundCloudClient: SoundCloudClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetItems =>
      log.debug(s"Get recommendations request for profile id: ${msg.profileId}")

      val scMusicItems = msg.contextData match {
        case Some(data) =>
          soundCloudClient.getLoadedTracks(data.ctxName)
        case None =>
          soundCloudClient.getLoadedTracks(Situation.Workout) // TODO: decide on this context
      }

      val musicItemsForResponse = scMusicItems.map { item =>
        MusicItem(item.id, item.title, item.username, item.permalinkUrl, item.duration, item.artworkUrl)
      }

      val musicOrderItems = musicItemsForResponse.map { item =>
        OrderItem("musicItems", item.id)
      }

      val videoOrderItems = (1 to 5).map { id =>
        OrderItem("videoItems", id)
      }

      val response = RecommendationsResponse(
        Random.shuffle(musicOrderItems ++ videoOrderItems),
        musicItemsForResponse,
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

  def props(soundCloudClient: SoundCloudClient): Props = {
    Props.create(classOf[RecommendationsActor], soundCloudClient)
  }

  case class GetItems(sprayCtx: RequestContext, profileId: Long, contextData: Option[ContextData])

}

