package me.numbereight.contextsharing.foursquare

import me.numbereight.contextsharing.model.ContextNames.Place
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

// TODO: use separate

case class LatLon(lat: Double, lon: Double) {
  def foursquareString: String = s"$lat,$lon"
}

object LatLon {
  def apply(str: String): LatLon = {
    val latLon = str.split(",")
    LatLon(latLon(0).toDouble, latLon(1).toDouble)
  }
}

class FoursquareClient(httpClient: WSClient) {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)

  def performLocationCategoryQuery(location: LatLon, place: String): Future[Option[VenueResponse]] = {
    FoursquareClient.ContextCategoryIds.get(place) match {
      case Some(categoryId) =>
        httpClient
          .url(FoursquareClient.VenueEndpoint)
          .withQueryString(
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
          .get()
          .map { response =>
            if (!(200 to 299).contains(response.status)) {
              log.error(s"Received unexpected status ${response.status} : ${response.body}")
            }
            Try(Serialization.read[VenueResponse](response.body)) match {
              case Success(res) => Some(res)
              case Failure(t) =>
                log.error("Unable to parse response from Foursquare", t)
                None
            }
          }
      case None => Future(None)
    }
  }

}


object FoursquareClient {
  val VenueEndpoint = "https://api.foursquare.com/v2/venues/search"
  val ClientSecret = "FMTHCUNEIHNIVZUOCWOZHZEFL33GII2NZNTV4Z3RLHZ23PTK"
  val ClientId = "NVLWVF0KX0EFP4CZLDCF3YLOBRY2L4VZXUGRUPBHW0ROT2YE"
  val Radius = 30.toString
  val Version = 20140503.toString
  val Locale = "en"
  val Limit = 1.toString
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
    Place.ShopsAndServices -> "4d4b7105d754a06378d81259", // Shops & Services
    Place.FoodRelated -> "4d4b7105d754a06374d81259" // Food (includes all types of restaurants)
  )

}