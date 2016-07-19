import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object TimezonesTest {

  def main(args: Array[String]) {

    val timezoneOffset = 180 // UTC+3
    val currTime = new DateTime().withTimeAtStartOfDay().getMillis
    val currTimeInUtc = new DateTime().withZone(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis
    val timeOfStartOfADay = new DateTime().withTimeAtStartOfDay().getMillis

    println(currTime)
    println(currTimeInUtc)
    println(timeOfStartOfADay)

  }

}
