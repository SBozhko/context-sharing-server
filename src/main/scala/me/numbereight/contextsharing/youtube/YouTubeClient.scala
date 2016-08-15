package me.numbereight.contextsharing.youtube
import java.net.URLEncoder
import java.time.Duration

import me.numbereight.contextsharing.model.ContextNames.Situation
import me.numbereight.contextsharing.model.VideoItem
import me.numbereight.contextsharing.model.YouTubeVideo
import me.numbereight.contextsharing.util.HttpClientUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.fluent.Request
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods.parse
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Random
import scala.util.Try

class YouTubeClient {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)
  val videos = mutable.Map[String, List[VideoItem]]() // TODO: Use Guava cache

  def populateVideos(): Unit = {
    var id = 0
    log.info("Started loading videos from YouTube")
    videos.clear()
    for (entry <- YouTubeClient.SituationTags;
         situationContext = entry._1;
         tag <- entry._2) {

      val ytVideos = getVideos(tag)
      val durations = getDuration(ytVideos)

      val videosWithDurtion = ytVideos.map { item =>
        id = id + 1
        VideoItem(id, item.title, item.permalink, durations.getOrElse(item.id, 0), item.artwork)
      }

      val existingVideos = videos.getOrElse(situationContext, List())
      videos.put(situationContext, videosWithDurtion ++ existingVideos)
    }
    log.info("Videos from YouTube loaded successfully")
  }

  def getVideos(tag: String): List[YouTubeVideo] = {

    val queryParams = Map(
      "part" -> "snippet",
      "q" -> URLEncoder.encode(tag, "UTF-8"),
      "type" -> "video",
      "videoCaption" -> "closedCaption",
      "maxResults" -> YouTubeClient.NumberOfRecommendations,
      "relevanceLanguage" -> "en",
      "videoDuration" -> "short",
      "chart" -> "mostPopular",
      "key" -> YouTubeClient.ApiKey)

    val queryString = HttpClientUtils.queryParams2String(queryParams)

    val httpResponse = Request.Get(s"${YouTubeClient.SearchEndpoint}?$queryString")
      .execute()
      .returnResponse()

    val jsonResponse = IOUtils.toString(httpResponse.getEntity.getContent)
    val statusCode = httpResponse.getStatusLine.getStatusCode

    statusCode match {
      case code if 200 until 300 contains code =>
        val parsedResponse = parse(jsonResponse, useBigDecimalForDouble = false, useBigIntForLong = false)

        val items = parsedResponse \ "items"

        items match {
          case JArray(videos: List[JObject]) =>
            videos.map { i =>
              val videoId = HttpClientUtils.parseAsString(i \ "id" \ "videoId")
              val permalink = s"https://www.youtube.com/watch?v=$videoId"
              val title = HttpClientUtils.parseAsString(i \ "snippet" \ "title")
              val artwork = HttpClientUtils.parseAsString(i \ "snippet" \ "thumbnails" \ "high" \ "url")
              YouTubeVideo(videoId, title, permalink, artwork)
            }
          case _ =>
            log.error(s"Unable to parse response from Foursquare. $jsonResponse")
            Nil
        }
      case _ =>
        log.error(s"Received unexpected status $statusCode. Response: $jsonResponse")
        Nil
    }
  }

  def getDuration(videos: List[YouTubeVideo]): Map[String, Int] = {
    val ids = videos.map(item => item.id).mkString(",")

    val queryParams = Map(
      "id" -> ids,
      "part" -> "contentDetails",
      "key" -> YouTubeClient.ApiKey)

    val queryString = HttpClientUtils.queryParams2String(queryParams)

    val httpResponse = Request.Get(s"${YouTubeClient.VideosEndpoint}?$queryString")
      .execute()
      .returnResponse()

    val jsonResponse = IOUtils.toString(httpResponse.getEntity.getContent)
    val statusCode = httpResponse.getStatusLine.getStatusCode

    statusCode match {
      case code if 200 until 300 contains code =>
        val parsedResponse = parse(jsonResponse, useBigDecimalForDouble = false, useBigIntForLong = false)

        val items = parsedResponse \ "items"
        Try {
          items match {
            case JArray(videos: List[JObject]) =>
              videos.map { i =>
                val id = HttpClientUtils.parseAsString(i \ "id")
                val duration = HttpClientUtils.parseAsString(i \ "contentDetails" \ "duration")
                val durationInMs = durationToMs(duration).toInt
                id -> durationInMs
              }.toMap
          }
        }.recover {
          case t: Throwable =>
            log.error(s"Unable to parse response from YouTube: $jsonResponse", t)
            Map.empty[String, Int]
        }.get
      case _ =>
        log.error(s"Unable to parse response from Foursquare. $jsonResponse")
        Map.empty
      case _ =>
        log.error(s"Received unexpected status $statusCode. Response: $jsonResponse")
        Map.empty
    }
  }

  def durationToMs(duration: String): Long = {
    Duration.parse(duration).toMillis
  }

  def getLoadedVideos(situationContextName: String): List[VideoItem] = {
    log.debug(s"Loading videos for context $situationContextName")
    Random.shuffle(videos.getOrElse(situationContextName, List())).take(YouTubeClient.NumberOfRecommendations)
  }
}

object YouTubeClient {
  val ApiKey = "AIzaSyCoZ25r-F2HizLLst-GaaH89PsE96ipC_8"
  val SearchEndpoint = "https://www.googleapis.com/youtube/v3/search"
  val VideosEndpoint = "https://www.googleapis.com/youtube/v3/videos"
  val DefaultArtwork = "http://tatznailz.com/wp-content/uploads/2014/07/youtube-logo-300x300.png"
  val NumberOfRecommendations = 5

  val SituationTags = Map(
    Situation.Housework -> List(
      "energetic",
      "catchy",
      "happy",
      "sweet",
      "guilty pleasure",
      "smile",
      "sing along",
      "positive",
      "fun",
      "quirky",
      "ambient"),
    Situation.Bedtime -> List(
      "relaxing",
      "calm",
      "mellow",
      "dreamy",
      "sad",
      "beautiful",
      "ballad",
      "meditative",
      "peaceful",
      "soothing",
      "relaxation",
      "instrumental"),
    Situation.Working -> List(
      "chill",
      "classical",
      "piano",
      "instrumental",
      "new age",
      "ambient",
      "beautiful",
      "mellow",
      "relax",
      "soundtrack",
      "composer",
      "studying",
      "working"),
    Situation.OnTheGo -> List(
      "steampunk",
      "dj",
      "driving",
      "progressive",
      "feel good",
      "groovy",
      "roadtrip",
      "super",
      "funky",
      "psycheledic"),
    Situation.Workout -> List(
      "dance",
      "electronic",
      "gym",
      "house",
      "fun",
      "club",
      "party",
      "mix",
      "mashups",
      "workout",
      "motivation",
      "running",
      "cardio"),
    Situation.Relaxing -> List(
      "relaxing",
      "atmospheric",
      "easy listening",
      "soothing",
      "lounge",
      "chill",
      "summer",
      "uplifting",
      "chillout",
      "beautiful",
      "beach",
      "sunny"),
    Situation.WakeUp -> List(
      "summer",
      "upbeat",
      "feel good",
      "awesome",
      "chill",
      "morning",
      "happy",
      "amazing",
      "acoustic",
      "fun"),
    Situation.Party -> List(
      "dance",
      "party",
      "pop",
      "upbeat",
      "club",
      "schlager",
      "house",
      "summer",
      "remix",
      "electro"))
}
