package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.actor.RecommendationsActor.GetItems
import me.numbereight.contextsharing.model.ContextData
import me.numbereight.contextsharing.model.ContextGroups
import me.numbereight.contextsharing.model.ContextNames.Situation
import me.numbereight.contextsharing.model.MusicItem
import me.numbereight.contextsharing.model.OrderItem
import me.numbereight.contextsharing.model.RecommendationsResponse
import me.numbereight.contextsharing.soundcloud.SoundCloudClient
import me.numbereight.contextsharing.youtube.YouTubeClient
import spray.http.StatusCodes
import spray.routing.RequestContext

import scala.util.Random

class RecommendationsActor(soundCloudClient: SoundCloudClient, youtubeClient: YouTubeClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetItems =>
      log.debug(s"Get recommendations request for profile id: ${msg.profileId}")

      val situation = msg.contextData.getOrElse(ContextData(ContextGroups.Situation, Situation.Workout)) // TODO: decide on this context

      val scMusicItems = soundCloudClient.getLoadedTracks(situation.ctxName)

      val musicItemsForResponse = scMusicItems.map { item =>
        MusicItem(item.id, item.title, item.username, item.permalinkUrl, item.duration, item.artworkUrl)
      }

      val musicOrderItems = musicItemsForResponse.map { item =>
        OrderItem("musicItems", item.id)
      }

      val videos = youtubeClient.getLoadedVideos(situation.ctxName)

      val videoOrderItems = videos.map { item =>
        OrderItem("videoItems", item.id)
      }

      val response = RecommendationsResponse(
        Random.shuffle(musicOrderItems ++ videoOrderItems),
        musicItemsForResponse,
        videos)

      sendResponse(msg.sprayCtx, StatusCodes.OK, response)
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object RecommendationsActor {

  val Name = "recommendationsActor"

  def props(soundCloudClient: SoundCloudClient, youtubeClient: YouTubeClient): Props = {
    Props.create(classOf[RecommendationsActor], soundCloudClient, youtubeClient)
  }

  case class GetItems(sprayCtx: RequestContext, profileId: Long, contextData: Option[ContextData])

}

