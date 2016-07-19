package me.numbereight.contextsharing.actor


import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.db.PostgresUserProfileClient
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsResponse
import me.numbereight.contextsharing.model.StatsPeriod
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import spray.http.StatusCodes

import scala.collection.mutable


class UserStatsActor(
  historyClient: PostgresContextHistoryClient,
  profileClient: PostgresUserProfileClient) extends BaseHttpServiceActor {

  val timezones = mutable.Map[(String, String), Int]()

  override def receive: Actor.Receive = {
    case msg: GetUserStatsActorRequest =>
      val timezoneInMins = timezones.get((msg.request.userId, msg.request.vendorId)) match {
        case Some(tz) => tz
        case None =>
          profileClient.getTimezoneOffset(msg.request.userId, msg.request.vendorId) match {
            case Some(tz) =>
              timezones.put((msg.request.userId, msg.request.vendorId), tz)
              tz
            case None =>
              log.warning(s"Unable to find timezone for stats: ${msg.request}. Applying UTC timezone.")
              0
          }
      }

      val lookupSinceMs = getTimestamp(msg.request.period, timezoneInMins)
      val filteredValues = historyClient.getStats(msg.request, lookupSinceMs)
      sendResponse(msg.sprayCtx, StatusCodes.OK, GetUserStatsResponse(filteredValues))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

  def getTimestamp(period: String, timezoneMins: Int): Long = {
    val currTimestamp = new DateTime().getMillis
    val midnightTimestampInUtc = period match {
      case StatsPeriod.Day =>
        new DateTime().withZone(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis
      case StatsPeriod.Week =>
        new LocalDate().dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
      case StatsPeriod.Month =>
        new LocalDate().dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
    }
    val diffWithTimezone = currTimestamp - midnightTimestampInUtc + timezoneMins * 60000
    currTimestamp - diffWithTimezone
  }

}

object UserStatsActor {

  val Name = "contextStorageActor"

  val UserStatsMap = ContextNames.SampleUserStats.map(item => item.ctxGroup -> item.values).toMap

  def props(historyClient: PostgresContextHistoryClient, profileClient: PostgresUserProfileClient): Props = {
    Props.create(classOf[UserStatsActor], historyClient, profileClient)
  }

}

