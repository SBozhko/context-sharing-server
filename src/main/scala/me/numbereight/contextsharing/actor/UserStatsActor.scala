package me.numbereight.contextsharing.actor


import akka.actor.Actor
import akka.actor.Props
import me.numbereight.contextsharing.db.DynamoDbClient
import me.numbereight.contextsharing.model.CtxPercentage
import me.numbereight.contextsharing.model.CtxStats
import me.numbereight.contextsharing.model.GetUserStatsActorRequest
import me.numbereight.contextsharing.model.GetUserStatsResponse
import spray.http.StatusCodes


class UserStatsActor(dynamoClient: DynamoDbClient) extends BaseHttpServiceActor {

  override def receive: Actor.Receive = {
    case msg: GetUserStatsActorRequest =>
      // TODO: go to DB and check stats
      val filteredValues = msg.request.ctxTypes.map(item => CtxStats(item, UserStatsActor.UserStatsMap(item)))
      sendResponse(msg.sprayCtx, StatusCodes.OK, GetUserStatsResponse(filteredValues))
      log.debug(s"Stored contextData: ${msg.request}")
    case something: Any =>
      handleUnknownMsg(something)
  }

}

object UserStatsActor {

  val Name = "contextStorageActor"

  val AllowedCtxTypes = Set(
    "Brightness",
    "Place",
    "Situation",
    "TimeOfDay",
    "DayOfWeek",
    "Weather",
    "IndoorOutdoor",
    "Activity",
    "Mood"
  )

  val UserStats = List(
    CtxStats("Brightness", List(
      CtxPercentage("Dark", 60.1f),
      CtxPercentage("Light", 39.9f)
    )),
    CtxStats("Place", List(
      CtxPercentage("Home", 4.1f),
      CtxPercentage("Office", 40.9f),
      CtxPercentage("Library", 0.0f),
      CtxPercentage("Gym", 15.0f),
      CtxPercentage("Beach", 0.0f),
      CtxPercentage("ShopsAndServices", 5.0f),
      CtxPercentage("FoodRelated", 35.0f)
    )),
    CtxStats("Situation", List(
      CtxPercentage("WakeUp", 4.0f),
      CtxPercentage("OnTheGo", 10.5f),
      CtxPercentage("Working", 30.5f),
      CtxPercentage("Workout", 5.0f),
      CtxPercentage("Party", 10.0f),
      CtxPercentage("Housework", 5.0f),
      CtxPercentage("Relaxing", 5.0f),
      CtxPercentage("Bedtime", 30.0f)
    )),
    CtxStats("TimeOfDay", List(
      CtxPercentage("EarlyHours", 4.0f),
      CtxPercentage("Morning", 11.0f),
      CtxPercentage("Breakfast", 30.0f),
      CtxPercentage("BeforeLunch", 5.0f),
      CtxPercentage("Lunch", 10.0f),
      CtxPercentage("Afternoon", 4.8f),
      CtxPercentage("Evening", 5.2f),
      CtxPercentage("Dinner", 20.0f),
      CtxPercentage("Night", 10.0f)
    )),
    CtxStats("DayOfWeek", List(
      CtxPercentage("WorkDay", 80.4f),
      CtxPercentage("RestDay", 19.6f)
    )),
    CtxStats("Weather", List(
      CtxPercentage("Normal", 4.0f),
      CtxPercentage("Sunny", 11.0f),
      CtxPercentage("Cloudy", 30.0f),
      CtxPercentage("Windy", 4.6f),
      CtxPercentage("Breezy", 10.4f),
      CtxPercentage("Snow", 0.0f),
      CtxPercentage("Rain", 0.0f),
      CtxPercentage("Drizzle", 30.0f),
      CtxPercentage("Thunderstorm", 0.0f),
      CtxPercentage("Extreme", 0.0f),
      CtxPercentage("VeryHot", 0.0f),
      CtxPercentage("Hot", 0.0f),
      CtxPercentage("Warm", 10.0f),
      CtxPercentage("Cold", 0.0f),
      CtxPercentage("Freezing", 0.0f)
    )),
    CtxStats("IndoorOutdoor", List(
      CtxPercentage("Indoor", 34.3f),
      CtxPercentage("Outdoor", 65.7f)
    )),
    CtxStats("Activity", List(
      CtxPercentage("Walking", 20.0f),
      CtxPercentage("Running", 10.7f),
      CtxPercentage("Driving", 9.3f),
      CtxPercentage("Stationary", 50.0f),
      CtxPercentage("Cycling", 10.0f)
    )),
    CtxStats("Mood", List(
      CtxPercentage("Happy", 40.4f),
      CtxPercentage("Angry", 20.0f),
      CtxPercentage("Sad", 29.6f),
      CtxPercentage("Calm", 10.0f)
    ))
  )

  val UserStatsMap = UserStats.map(item => item.ctxType -> item.values).toMap

  def props(dynamoClient: DynamoDbClient): Props = {
    Props.create(classOf[UserStatsActor], dynamoClient)
  }

}

