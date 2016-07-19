package me.numbereight.contextsharing.db

import me.numbereight.contextsharing.model.SubmitUserInfoMessage
import org.slf4j.LoggerFactory
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

class PostgresUserProfileClient(cpName: String) {
  val log = LoggerFactory.getLogger(getClass)

  def saveUserProfile(userInfo: SubmitUserInfoMessage): Option[Long] = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        val insert =
          sql"""
                INSERT INTO user_profiles (user_id, vendor_id, advertising_id, timezone_offset_minutes) VALUES(
                ${userInfo.userId},
                ${userInfo.vendorId},
                ${userInfo.advertisingId},
                ${userInfo.timezoneOffsetMins}
                )
        """
        Option(insert.updateAndReturnGeneratedKey().apply())
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to store user into $userInfo", e)
        None
    }
  }


}
