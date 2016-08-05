package me.numbereight.contextsharing.foursquare

import me.numbereight.contextsharing.model.ContextNames.Place
import me.numbereight.contextsharing.util.HttpClientUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.fluent.Request
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JArray
import org.json4s.JsonAST.JInt
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

import scala.util.Try

case class LatLong(lat: Double, lon: Double) {
  def foursquareString: String = s"$lat,$lon"
}

object LatLong {
  def apply(str: String): LatLong = {
    val latLon = str.split(",")
    LatLong(latLon(0).toDouble, latLon(1).toDouble)
  }
}

class FoursquareClient {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)

  def performLocationCategoryQuery(location: LatLong, place: String): Option[Int] = {
    FoursquareClient.ContextCategoryIds.get(place) match {
      case Some(categoryId) =>
        val queryParams = Map(
          "locale" -> FoursquareClient.Locale,
          "limit" -> FoursquareClient.Limit,
          "client_secret" -> FoursquareClient.ClientSecret,
          "intent" -> FoursquareClient.Intent,
          "m" -> FoursquareClient.Mode,
          "ll" -> location.foursquareString,
          "categoryId" -> categoryId,
          "v" -> FoursquareClient.Version,
          "client_id" -> FoursquareClient.ClientId,
          "radius" -> FoursquareClient.Radius)
        val queryString = HttpClientUtils.queryParams2String(queryParams)

        val httpResponse = Request.Get(s"${FoursquareClient.VenueEndpoint}?$queryString")
          .execute()
          .returnResponse()

        val jsonResponse = IOUtils.toString(httpResponse.getEntity.getContent)
        val statusCode = httpResponse.getStatusLine.getStatusCode

        statusCode match {
          case code if 200 until 300 contains code =>
            val parsedResponse = parse(jsonResponse, useBigDecimalForDouble = false, useBigIntForLong = false)
            Try {
              parsedResponse \ "response" \ "venues" match {
                case JArray(List()) =>
                  None
                case JArray(List(venue)) =>
                  val distance = venue \ "location" \ "distance"
                  distance match {
                    case JInt(distanceVal) =>
                      Some(distanceVal.intValue())
                    case _ =>
                      log.error(s"Unable to parse distance from Foursquare response. $jsonResponse")
                      None
                  }
                case _ =>
                  log.error(s"Unable to parse response from Foursquare. $jsonResponse")
                  None
              }
            }.recover {
              case t: Throwable =>
                log.error(s"Unable to parse response from Foursquare. $jsonResponse", t)
                None
            }.get
          case _ =>
            log.error(s"Received unexpected status $statusCode. Response: $jsonResponse")
            None
        }
      case None =>
        log.error(s"Unknown place: $place. No categoryId")
        None
    }
  }

}


object FoursquareClient {
  val VenueEndpoint = "https://api.foursquare.com/v2/venues/search"
  val ClientSecret = "FMTHCUNEIHNIVZUOCWOZHZEFL33GII2NZNTV4Z3RLHZ23PTK"
  val ClientId = "NVLWVF0KX0EFP4CZLDCF3YLOBRY2L4VZXUGRUPBHW0ROT2YE"
  val Radius = 20
  val Version = 20140503
  val Locale = "en"
  val Limit = 1
  val Intent = "checkin"
  val Mode = "foursquare"
  val ContextCategoryIds = Map(
    Place.Office -> List(
      "4bf58dd8d48988d124941735", // Office
      "4bf58dd8d48988d13b941735", // School
      "50328a8e91d4c4b30a586d6c").mkString(","), // Non-Profit
    Place.Home -> "4e67e38e036454776db1fb3a", // Residential
    Place.Gym -> List(
      "4bf58dd8d48988d1b4941735", // College Stadium
      "4bf58dd8d48988d1a9941735", // College Rec Center
      "4bf58dd8d48988d1b2941735", // College Gym
      "4bf58dd8d48988d175941735", // Gym/Fitness Center
      "4f4528bc4b90abdf24c9de85", // Athletics & Sports
      "4bf58dd8d48988d163941735", // Park
      "52e81612bcbc57f1066b7a26", // Rafting
      "50328a4b91d4c4b30a586d6b", // Recreation Center
      "4bf58dd8d48988d1e9941735", // Rock Climbing Spot
      "52e81612bcbc57f1066b7a29").mkString(","), // Ski Area
    Place.Library -> List(
      "4bf58dd8d48988d12f941735", // Library
      "4bf58dd8d48988d1a7941735", // College Library
      "4bf58dd8d48988d198941735", // College Academic Building
      "4bf58dd8d48988d1a0941735").mkString(","), // College Classroom
    Place.Beach -> "4bf58dd8d48988d1e2941735,52e81612bcbc57f1066b7a0d", // Beach, Beach Bar
    Place.Shop -> "4d4b7105d754a06378d81259", // Shops & Services
    Place.Restaurant -> "4d4b7105d754a06374d81259" // Food (includes all types of restaurants)
  )

}