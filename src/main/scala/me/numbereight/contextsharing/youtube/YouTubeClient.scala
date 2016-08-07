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
              val artwork = HttpClientUtils.parseAsString(i \ "snippet" \ "thumbnails" \ "medium" \ "url")
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
    Situation.Housework -> List("Housework", "Fun", "Feel good", "Positive vibes", "Upbeat", "Classic", "Chores", "Energy", "Laundry", "Cleaning"),
    Situation.Relaxing -> List("Chill", "Relax", "Acoustic", "Happy", "Summer", "Throwback", "Beach", "Nature", "Instrumental", "Calm"),
    Situation.Working -> List("Classical", "Ambient", "Piano", "Concentration", "Focus", "Reading", "Studying", "Instrumental", "Exam", "Soundtrack"),
    Situation.OnTheGo -> List("Upbeat", "Cruising", "Jamming", "Adventure", "Driving", "Car", "Holiday", "Party", "Roadtrip", "Retro"),
    Situation.Workout -> List("Gym", "Fitness", "Workout", "Remix", "Dance", "Mashup", "Running", "Party", "Pump up", "Club", "Cardio"),
    Situation.Relaxing -> List("Meditation", "Classical", "Nature", "Sad", "Acoustic", "Sleep", "Slow", "Sex", "Tired", "Soft"),
    Situation.WakeUp -> List("Wake up", "Morning", "Motivation", "Coffee", "Good vibes", "Sunshine", "Rain", "Cheer", "Get ready", "Awake"))

}
