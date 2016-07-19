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
                ${userInfo.timezoneOffsetMins})
            """
        Option(insert.updateAndReturnGeneratedKey().apply())
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to store user into $userInfo", e)
        None
    }
  }

  def getUserProfileId(advertisingId: String): Option[Long] = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        val select =
          sql"""
                SELECT id FROM user_profiles
                WHERE advertising_id = $advertisingId
            """
        select.map(rs => rs.long("advertising_id")).single().apply()
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to get user profile id by advertising id $advertisingId", e)
        None
    }
  }

  def getTimezoneOffset(profileId: Long): Option[Int] = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        val select =
          sql"""
                SELECT timezone_offset_minutes FROM user_profiles
                WHERE id = $profileId
            """
        select.map(rs => rs.int("timezone_offset_minutes")).single().apply()
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to get timezone for user with profileId=$profileId", e)
        None
    }
  }


}
