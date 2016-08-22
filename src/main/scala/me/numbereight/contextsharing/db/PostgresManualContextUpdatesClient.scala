package me.numbereight.contextsharing.db

import java.util.concurrent.TimeUnit

import me.numbereight.contextsharing.model.ContextDataPair
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import scala.concurrent.duration.FiniteDuration

class PostgresManualContextUpdatesClient(cpName: String) {

  private val DurationToOverrideContextMillis = FiniteDuration(1, TimeUnit.HOURS).toMillis
  private val log = LoggerFactory.getLogger(getClass)

  def saveContextData(profileId: Long, ctxDataList: List[ContextDataPair]): Unit = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val date = new DateTime().getMillis

        ctxDataList.foreach { ctxPair =>
          val insert =
            sql"""
                INSERT INTO manual_context_updates (user_profile_id, context_group, context_name, context_updated_at_unix_time)
                VALUES ($profileId, ${ctxPair.ctxGroup}, ${ctxPair.ctxName}, $date)
            """
          insert.updateAndReturnGeneratedKey().apply()
        }
      }
    } catch {
      case e: Exception =>
        log.error("Unable to store context data", e)
    }
  }

  def getManualContextData(profileId: Long, ctxGroup: String): Option[String] = {
    try {
      NamedDB(cpName) localTx { implicit session =>


        val timestampOfInsert = new DateTime().getMillis - DurationToOverrideContextMillis

        val select =
          sql"""
                SELECT context_name, context_updated_at_unix_time FROM manual_context_updates
                WHERE user_profile_id = $profileId
                AND context_group = $ctxGroup
                AND context_updated_at_unix_time > $timestampOfInsert
                ORDER BY context_updated_at_unix_time DESC
                LIMIT 1;
            """
        select.map(rs => rs.string("context_name")).single().apply()
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to get manually updated context data for user with profileId: $profileId", e)
        None
    }
  }

}
