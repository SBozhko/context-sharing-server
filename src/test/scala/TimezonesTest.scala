import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate

object TimezonesTest {

  def main(args: Array[String]) {

    val timezoneOffset = 180 // UTC+3
    val currTime = new DateTime().withTimeAtStartOfDay().getMillis
    val currTimeInUtc = new DateTime().withZone(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis
    val timeOfStartOfADay = new DateTime().withTimeAtStartOfDay().getMillis




    val startOfWeek = new LocalDate().dayOfWeek().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
    val startOfMonth = new LocalDate().dayOfMonth().withMinimumValue().toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis
    //val startedOfWeekUnixTime = new DateTime(startOfWeek)

    println(startOfMonth)
    //println(startedOfWeekUnixTime)


    //println(currTime)
    //println(currTimeInUtc)
    //println(timeOfStartOfADay)

  }

}
