package me.numbereight.contextsharing.model

trait Contexts {
  val All: Set[String]

  def valid(ctxName: String): Boolean = All.contains(ctxName)
}

object ContextGroups extends Contexts {

  val Situation = "Situation"
  val TimeOfDay = "TimeOfDay"
  val DayCategory = "DayCategory"
  val Weather = "Weather"
  val Loudness = "Loudness"
  val Lightness = "Lightness"
  val IndoorOutdoor = "IndoorOutdoor"
  val Activity = "Activity"
  val Mood = "Mood"
  val Place = "Place"

  override val All = Set(
    Situation, TimeOfDay, DayCategory, Weather, Loudness, Lightness, IndoorOutdoor, Activity, Mood, Place
  )
}

object ContextNames extends Contexts {

  val SampleUserStats = List(
    CtxStats(ContextGroups.Lightness, List(
      CtxPercentage(Lightness.Dark, 60.1f),
      CtxPercentage(Lightness.Light, 39.9f)
    )),
    CtxStats(ContextGroups.Place, List(
      CtxPercentage(Place.Home, 4.1f),
      CtxPercentage(Place.Office, 40.9f),
      CtxPercentage(Place.Library, 0.0f),
      CtxPercentage(Place.Gym, 15.0f),
      CtxPercentage(Place.Beach, 0.0f),
      CtxPercentage(Place.Shop, 5.0f),
      CtxPercentage(Place.Restaurant, 35.0f),
      CtxPercentage(Place.Other, 0.0f)
    )),
    CtxStats(ContextGroups.Situation, List(
      CtxPercentage(Situation.WakeUp, 4.0f),
      CtxPercentage(Situation.OnTheGo, 10.5f),
      CtxPercentage(Situation.Working, 30.5f),
      CtxPercentage(Situation.Workout, 5.0f),
      CtxPercentage(Situation.Party, 10.0f),
      CtxPercentage(Situation.Housework, 5.0f),
      CtxPercentage(Situation.Relaxing, 5.0f),
      CtxPercentage(Situation.Bedtime, 30.0f),
      CtxPercentage(Situation.Unknown, 30.0f)
    )),
    CtxStats(ContextGroups.TimeOfDay, List(
      CtxPercentage(TimeOfDay.EarlyHours, 4.0f),
      CtxPercentage(TimeOfDay.Morning, 11.0f),
      CtxPercentage(TimeOfDay.Breakfast, 30.0f),
      CtxPercentage(TimeOfDay.BeforeLunch, 5.0f),
      CtxPercentage(TimeOfDay.Lunch, 10.0f),
      CtxPercentage(TimeOfDay.Afternoon, 4.8f),
      CtxPercentage(TimeOfDay.Evening, 5.2f),
      CtxPercentage(TimeOfDay.Dinner, 20.0f),
      CtxPercentage(TimeOfDay.Night, 10.0f)
    )),
    CtxStats(ContextGroups.DayCategory, List(
      CtxPercentage(DayCategory.WorkDay, 80.4f),
      CtxPercentage(DayCategory.RestDay, 19.6f)
    )),
    CtxStats(ContextGroups.Weather, List(
      CtxPercentage(Weather.Normal, 4.0f),
      CtxPercentage(Weather.Sunny, 11.0f),
      CtxPercentage(Weather.Cloudy, 30.0f),
      CtxPercentage(Weather.Windy, 4.6f),
      CtxPercentage(Weather.Breezy, 10.4f),
      CtxPercentage(Weather.Snow, 0.0f),
      CtxPercentage(Weather.Rain, 0.0f),
      CtxPercentage(Weather.Drizzle, 30.0f),
      CtxPercentage(Weather.Thunderstorm, 0.0f),
      CtxPercentage(Weather.Extreme, 0.0f),
      CtxPercentage(Weather.VeryHot, 0.0f),
      CtxPercentage(Weather.Hot, 0.0f),
      CtxPercentage(Weather.Warm, 10.0f),
      CtxPercentage(Weather.Cold, 0.0f),
      CtxPercentage(Weather.Freezing, 0.0f)
    )),
    CtxStats(ContextGroups.IndoorOutdoor, List(
      CtxPercentage(IndoorOutdoor.Indoor, 34.3f),
      CtxPercentage(IndoorOutdoor.Outdoor, 65.7f)
    )),
    CtxStats(ContextGroups.Activity, List(
      CtxPercentage(Activity.Walking, 20.0f),
      CtxPercentage(Activity.Running, 10.7f),
      CtxPercentage(Activity.Driving, 9.3f),
      CtxPercentage(Activity.Stationary, 50.0f),
      CtxPercentage(Activity.Cycling, 10.0f)
    )),
    CtxStats(ContextGroups.Mood, List(
      CtxPercentage(Mood.Happy, 40.4f),
      CtxPercentage(Mood.Angry, 20.0f),
      CtxPercentage(Mood.Sad, 29.6f),
      CtxPercentage(Mood.Calm, 10.0f)
    )),
    CtxStats(ContextGroups.Loudness, List(
      CtxPercentage(Loudness.Noisy, 40.4f),
      CtxPercentage(Loudness.Quiet, 60.0f)
    ))
  )

  object Lightness extends Contexts {
    val Dark = "Dark"
    val Light = "Light"
    override val All = Set(Dark, Light)
  }

  object TimeOfDay extends Contexts {
    val EarlyHours = "EarlyHours"
    val Morning = "Morning"
    val Breakfast = "Breakfast"
    val BeforeLunch = "BeforeLunch"
    val Lunch = "Lunch"
    val Afternoon = "Afternoon"
    val Evening = "Evening"
    val Dinner = "Dinner"
    val Night = "Night"
    override val All = Set(EarlyHours, Morning, Breakfast, BeforeLunch, Lunch, Afternoon, Evening, Dinner, Night)
  }

  object Loudness extends Contexts {
    val Noisy = "Noisy"
    val Quiet = "Quiet"
    override val All = Set(Noisy, Quiet)
  }

  object DayCategory extends Contexts {
    val WorkDay = "WorkDay"
    val RestDay = "RestDay"
    override val All = Set(WorkDay, RestDay)
  }

  object Weather extends Contexts {
    val Normal = "Normal"
    val Sunny = "Sunny"
    val Cloudy = "Cloudy"
    val Windy = "Windy"
    val Breezy = "Breezy"
    val Snow = "Snow"
    val Rain = "Rain"
    val Drizzle = "Drizzle"
    val Thunderstorm = "Thunderstorm"
    val Extreme = "Extreme"
    val VeryHot = "VeryHot"
    val Hot = "Hot"
    val Warm = "Warm"
    val Cold = "Cold"
    val Freezing = "Freezing"
    override val All = Set(Normal, Sunny, Cloudy, Windy, Breezy, Snow, Rain, Drizzle, Thunderstorm, Extreme,
      VeryHot, Hot, Warm, Cold, Freezing)
  }

  object IndoorOutdoor extends Contexts {
    val Indoor = "Indoor"
    val Outdoor = "Outdoor"
    override val All = Set(Indoor, Outdoor)
  }

  object Activity extends Contexts {
    val Walking = "Walking"
    val Running = "Running"
    val Driving = "Driving"
    val Stationary = "Stationary"
    val Cycling = "Cycling"
    override val All = Set(Walking, Running, Driving, Stationary, Cycling)
  }

  object Mood extends Contexts {
    val Happy = "Happy"
    val Angry = "Angry"
    val Sad = "Sad"
    val Calm = "Calm"
    override val All = Set(Happy, Angry, Sad, Calm)
  }

  object Place extends Contexts {
    val Home = "Home"
    val Office = "Office"
    val Library = "Library"
    val Gym = "Gym"
    val Beach = "Beach"
    val Shop = "Shop"
    val Restaurant = "Restaurant"
    val Other = "Other"
    override val All = Set(Home, Office, Library, Gym, Beach, Shop, Restaurant, Other)
    val AllMeaningful = Set(Home, Office, Library, Gym, Beach, Shop, Restaurant)
  }

  object Situation extends Contexts {
    val WakeUp = "WakeUp"
    val OnTheGo = "OnTheGo"
    val Working = "Working"
    val Workout = "Workout"
    val Party = "Party"
    val Housework = "Housework"
    val Relaxing = "Relaxing"
    val Bedtime = "Bedtime"
    val Unknown = "Unknown"
    override val All = Set(WakeUp, OnTheGo, Working, Workout, Party, Housework, Relaxing, Bedtime, Unknown)
  }

  override val All: Set[String] = Set(Situation, TimeOfDay, DayCategory, Weather, Loudness, Lightness, IndoorOutdoor,
    Activity, Mood, Place).foldLeft(Set[String]())((all: Set[String], ctxName: Contexts) => all ++ ctxName.All)

  def valid(ctxGroup: String, ctxName: String): Boolean = {
    ContextGroups.valid(ctxGroup) match {
      case true => ctxGroupByString(ctxGroup).valid(ctxName)
      case false => false
    }
  }

  def ctxGroupByString(ctxGroup: String): Contexts = {
    ctxGroup match {
      case ContextGroups.Situation => ContextNames.Situation
      case ContextGroups.TimeOfDay => ContextNames.TimeOfDay
      case ContextGroups.DayCategory => ContextNames.DayCategory
      case ContextGroups.Weather => ContextNames.Weather
      case ContextGroups.Loudness => ContextNames.Loudness
      case ContextGroups.Lightness => ContextNames.Lightness
      case ContextGroups.IndoorOutdoor => ContextNames.IndoorOutdoor
      case ContextGroups.Activity => ContextNames.Activity
      case ContextGroups.Mood => ContextNames.Mood
      case ContextGroups.Place => ContextNames.Place
    }
  }
}



