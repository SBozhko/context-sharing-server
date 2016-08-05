package me.numbereight.contextsharing.db

import me.numbereight.contextsharing.model.PlaceEvent
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

class PostgresPlaceHistoryClient(cpName: String) {
  val log = LoggerFactory.getLogger(getClass)

  def saveContextData(placeEvent: PlaceEvent): Unit = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val date = new DateTime().getMillis
        val insert =
          sql"""
                INSERT INTO place_history (vendor_id, latitude, longitude, place, check_in_at_unix_time) VALUES(
                ${placeEvent.vendorId},
                ${placeEvent.latLong.lat},
                ${placeEvent.latLong.long},
                ${placeEvent.place},
                $date)
            """
        insert.updateAndReturnGeneratedKey().apply()
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to store place event $placeEvent", e)
    }
  }


}
