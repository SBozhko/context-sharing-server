package me.numbereight.contextsharing.soundcloud

import java.net.URLEncoder

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

class SoundCloudClient {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)
  val tracks = mutable.Map[String, List[SoundCloudTrack]]() // TODO: Use Guava cache

  def populateTracks(): Unit = {
    log.info("Started loading tracks from SoundCloud")
    tracks.clear()
    for (entry <- SoundCloudClient.SituationTags;
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
    Random.shuffle(tracks.getOrElse(situationContextName, List())).take(SoundCloudClient.NumberOfRecommendations)
  }

  def getTracks(tag: String): List[SoundCloudTrack] = {
    val queryParams = Map(
      "client_id" -> SoundCloudClient.ClientId,
      "filter" -> SoundCloudClient.Filter,
      "duration-from" -> SoundCloudClient.DurationFrom,
      "duration-to" -> SoundCloudClient.DurationTo,
      "limit" -> SoundCloudClient.Limit,
      "order" -> SoundCloudClient.Order,
      "tags" -> URLEncoder.encode(tag, "UTF-8"))

    val queryString = HttpClientUtils.queryParams2String(queryParams)

    val httpResponse = Request.Get(s"${SoundCloudClient.Url}?$queryString")
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
                  case JNull => SoundCloudClient.DefaultArtwork
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


object SoundCloudClient {
  val Url = "https://api.soundcloud.com/tracks.json"
  val ClientId = "580e143e5540bfe1a6de0fcb4f9cf26e"
  val Filter = "streamable"
  val DurationFrom = 150000
  val DurationTo = 1800000
  val Limit = 10
  val Order = "hotness"
  val DefaultArtwork = "http://media-assets-04.thedrum.com/cache/images/thedrum-prod/news-tmp-116055-soundcloud-logo--default--300.png"

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

  val NumberOfRecommendations = 5

}