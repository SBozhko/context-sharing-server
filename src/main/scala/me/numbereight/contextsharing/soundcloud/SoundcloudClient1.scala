package me.numbereight.contextsharing.soundcloud

import java.net.URLEncoder

import me.numbereight.contextsharing.model.ContextNames.Mood
import me.numbereight.contextsharing.model.ContextNames.Situation
import me.numbereight.contextsharing.model.SoundCloudTrack
import me.numbereight.contextsharing.util.HttpClientUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.fluent.Request
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JNull
import org.json4s.JsonAST.JObject
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods.parse
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Random
import scala.util.Try

class SoundcloudClient {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)
  val tracks = mutable.Map[String, List[SoundCloudTrack]]() // TODO: Use Guava cache

  def populateTracks(): Unit = {
    log.info("Started loading tracks from SoundCloud")
    tracks.clear()
    for (entry <- SoundcloudClient.SituationTags;
         situationContext = entry._1;
         tag <- entry._2) {

      val scTracks = getTracks(tag)

      val existingTracks = tracks.getOrElse(situationContext, List())
      tracks.put(situationContext, scTracks ++ existingTracks)
    }
    log.info("Tracks from SoundCloud were loaded successfully")
  }

  def getLoadedTracks(situationContextName: String): List[SoundCloudTrack] = {
    log.debug(s"Loading tracks for context $situationContextName")
    Random.shuffle(tracks.getOrElse(situationContextName, List())).take(SoundcloudClient.NumberOfRecommendations)
  }

  def getTracks(tag: String): List[SoundCloudTrack] = {
    val queryParams = Map(
      "client_id" -> SoundcloudClient.ClientId,
      "filter" -> SoundcloudClient.Filter,
      "duration-from" -> SoundcloudClient.DurationFrom,
      "duration-to" -> SoundcloudClient.DurationTo,
      "limit" -> SoundcloudClient.Limit,
      "order" -> SoundcloudClient.Order,
      "tags" -> URLEncoder.encode(tag, "UTF-8"))

    val queryString = HttpClientUtils.queryParams2String(queryParams)

    val httpResponse = Request.Get(s"${SoundcloudClient.Url}?$queryString")
      .execute()
      .returnResponse()

    val jsonResponse = IOUtils.toString(httpResponse.getEntity.getContent)
    val statusCode = httpResponse.getStatusLine.getStatusCode

    statusCode match {
      case code if 200 until 300 contains code =>
        val parsedResponse = parse(jsonResponse, useBigDecimalForDouble = false, useBigIntForLong = false)

        parsedResponse match {
          case JArray(List()) =>
            Nil
          case JArray(tracks: List[JObject]) =>
            tracks.flatMap { item =>
              Try {
                val id = HttpClientUtils.parseAsLong(item \ "id")
                val duration = HttpClientUtils.parseAsLong(item \ "duration").toInt
                val permalink = HttpClientUtils.parseAsString(item \ "permalink_url")
                val artwork = item \ "artwork_url" match {
                  case JString(value) => value.replaceFirst("-large.", "-t300x300.")
                  case JNull => SoundcloudClient.DefaultArtwork
                  case unknown => throw new IllegalArgumentException(s"Unexpected value for json field: $unknown")
                }
                val title = HttpClientUtils.parseAsString(item \ "title")
                val username = HttpClientUtils.parseAsString(item \ "user" \ "username")
                Some(new SoundCloudTrack(id, duration, permalink, artwork, title, username))
              }.recover {
                case t: Throwable =>
                  log.error("Unable to parse response from SoundCloud", t)
                  None
              }.get
            }
          case _ =>
            log.error(s"Unable to parse response from SoundCloud. $jsonResponse")
            Nil
        }
      case _ =>
        log.error(s"Received unexpected status $statusCode. Response: $jsonResponse")
        Nil
    }
  }
}


object SoundcloudClient {
  val Url = "https://api.soundcloud.com/tracks.json"
  val ClientId = "580e143e5540bfe1a6de0fcb4f9cf26e"
  val Filter = "streamable"
  val DurationFrom = 150000
  val DurationTo = 1800000
  val Limit = 10
  val Order = "hotness"
  val DefaultArtwork = "http://media-assets-04.thedrum.com/cache/images/thedrum-prod/news-tmp-116055-soundcloud-logo--default--300.png"

  val MoodTags = Map(
    Mood.Happy -> List("Happy", "Playful", "Fun", "Excited", "Surprised", "Cheerful", "Bright", "Merry", "Majestic", "Joy"),
    Mood.Angry -> List("Angry", "Intense", "Heavy", "Aggressive", "Fear", "Fiery", "Anger", "Boisterous", "Rousing", "Dark", "Scary"),
    Mood.Sad -> List("Sad", "Depressing", "Melancholy", "Bittersweet", "Sentimental", "Gloomy", "Tragic", "Anxious", "Sadness", "Pathetic", "Mysterious"),
    Mood.Calm -> List("Calm", "Dreamy", "Soothing", "Sleepy", "Quiet", "Spiritual", "Tranquil", "Tender", "Serene", "Relaxed"))

  val SituationTags = Map(
    Situation.Housework -> List("Housework", "Fun", "Feel good", "Positive vibes", "Upbeat", "Classic", "Chores", "Energy", "Laundry", "Cleaning"),
    Situation.Relaxing -> List("Chill", "Relax", "Acoustic", "Happy", "Summer", "Throwback", "Beach", "Nature", "Instrumental", "Calm"),
    Situation.Working -> List("Classical", "Ambient", "Piano", "Concentration", "Focus", "Reading", "Studying", "Instrumental", "Exam", "Soundtrack"),
    Situation.OnTheGo -> List("Upbeat", "Cruising", "Jamming", "Adventure", "Driving", "Car", "Holiday", "Party", "Roadtrip", "Retro"),
    Situation.Workout -> List("Gym", "Fitness", "Workout", "Remix", "Dance", "Mashup", "Running", "Party", "Pump up", "Club", "Cardio"),
    Situation.Relaxing -> List("Meditation", "Classical", "Nature", "Sad", "Acoustic", "Sleep", "Slow", "Sex", "Tired", "Soft"),
    Situation.WakeUp -> List("Wake up", "Morning", "Motivation", "Coffee", "Good vibes", "Sunshine", "Rain", "Cheer", "Get ready", "Awake"))

  val NumberOfRecommendations = 5

}