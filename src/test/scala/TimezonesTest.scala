import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object TimezonesTest {

  def main(args: Array[String]) {
    val q = new DateTime().withZone(DateTimeZone.forOffsetMillis(10800000)).withTimeAtStartOfDay().getMillis

    println(q)

  }

}
