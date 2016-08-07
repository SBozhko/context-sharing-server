package me.numbereight.contextsharing.db

import me.numbereight.contextsharing.foursquare.LatLong
import me.numbereight.contextsharing.model.PlaceEvent
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

class PostgresPlaceDictionaryClient(cpName: String) {
  val log = LoggerFactory.getLogger(getClass)

  def savePlaceData(placeEvent: PlaceEvent): Unit = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val date = new DateTime().getMillis
        val insert =
          sql"""
                INSERT INTO place_dictionary (vendor_id, latitude, longitude, place, submit_at_unix_time) VALUES(
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

  def getPlaceData(latLong: LatLong, vendorId: Option[String]): List[String] = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val select =
        // http://johanndutoit.net/searching-in-a-radius-using-postgres/
          sql"""
                SELECT place FROM
                (SELECT a.place, earth_distance(ll_to_earth(${latLong.lat}, ${latLong.long}), ll_to_earth(a.latitude, a.longitude))
                AS distance_from_current_location
                FROM place_dictionary a
                ORDER BY distance_from_current_location ASC)
                AS results
                WHERE distance_from_current_location < 20;
            """
        select.map(rs => rs.string("place")).list().apply()
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to get place by coordinates: $latLong from place dictionary", e)
        Nil
    }
  }


}
