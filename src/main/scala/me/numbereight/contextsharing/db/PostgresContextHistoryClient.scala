package me.numbereight.contextsharing.db

import me.numbereight.contextsharing.model.ContextDataPair
import me.numbereight.contextsharing.model.ContextGroups
import me.numbereight.contextsharing.model.CtxPercentage
import me.numbereight.contextsharing.model.CtxStats
import me.numbereight.contextsharing.model.GetUserStatsRequest
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

import scala.collection.mutable

class PostgresContextHistoryClient(cpName: String) {

  val log = LoggerFactory.getLogger(getClass)

  def saveContextData(profileId: Long, contextData: List[ContextDataPair]): Unit = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val date = new DateTime().getMillis

        contextData.foreach { ctxPair =>
          val insert =
            sql"""
                INSERT INTO context_history (user_profile_id, context_group, context_name, context_started_at_unix_time)
                VALUES (${profileId}, ${ctxPair.ctxGroup}, ${ctxPair.ctxName}, $date)
            """
          insert.updateAndReturnGeneratedKey().apply()
        }
      }
    } catch {
      case e: Exception =>
        log.error("Unable to store context data", e)
    }
  }

  def getLastContextData(profileId: Long): Option[ContextDataPair] = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val select =
          sql"""
                SELECT id, context_name FROM context_history
                WHERE user_profile_id = $profileId
                AND context_group = 'Situation'
                ORDER BY context_started_at_unix_time DESC
                LIMIT 1;
            """
        val result: Option[(String)] = select.map { rs =>
          rs.string("context_name")
        }.single().apply()

        result match {
          case Some(value) => Some(ContextDataPair(ContextGroups.Situation, value))
          case _ =>
            log.warn(s"No context data for user with profileId: $profileId")
            None
        }
      }
    } catch {
      case e: Exception =>
        log.error(s"Unable to get context data for user with profileId: $profileId", e)
        None
    }
  }

  def getStats(request: GetUserStatsRequest, sinceUnixTime: Long): List[CtxStats] = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        request.ctxGroups.map { ctxGroup =>
          val select =
            sql"""
                SELECT context_name, context_started_at_unix_time FROM context_history
                WHERE user_profile_id = ${request.profileId}
                AND context_group = $ctxGroup
                AND context_started_at_unix_time >= $sinceUnixTime
                ORDER BY context_started_at_unix_time ASC
            """
          val result: List[(String, Long)] = select.map { rs =>
            rs.string("context_name") -> rs.long("context_started_at_unix_time")
          }.list().apply()

          result match {
            case Nil => CtxStats(ctxGroup, List.empty)
            case _ =>
              val currentTimeStamp = new DateTime().getMillis
              val intervals = new mutable.HashMap[String, Long]()

              result.reverse.zipWithIndex.foreach { (item: ((String, Long), Int)) =>
                val id = item._2
                val ctx = item._1

                if (id == 0) {
                  val diff = currentTimeStamp - ctx._2
                  intervals.put(ctx._1, diff)
                } else {
                  val diff = result.reverse.lift(id - 1).get._2 - ctx._2
                  val prevDiff = intervals.getOrElse(ctx._1, 0l)
                  intervals.put(ctx._1, diff + prevDiff)
                }
              }

              val totalDuration = currentTimeStamp - result.head._2

              val percentages = intervals.map { (item: (String, Long)) =>
                val percent = item._2.toDouble / totalDuration * 100
                new CtxPercentage(item._1, percent)
              }
              CtxStats(ctxGroup, percentages.toList)
          }
        }
      }
    } catch {
      case e: Exception =>
        log.error("Unable to count user stats", e)
        Nil
    }
  }


}
