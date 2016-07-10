package me.numbereight.contextsharing.db

import me.numbereight.contextsharing.model.SubmitContextRequest
import org.joda.time.DateTime
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

class PostgresContextHistoryClient(cpName: String) {

  def isAlive: Boolean = {
    true
  }

  def initDb() = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        sql"""
              CREATE TABLE  IF NOT EXISTS public.context_history	(
                id serial primary key,
                user_id varchar(50),
                advertising_id varchar(50),
                vendor_id varchar(50),
                context_group varchar(20),
                context_name varchar(20),
                context_started_at_unix_time bigint
              )
          """.execute.apply()
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def saveContextData(request: SubmitContextRequest): Unit = {
    try {
      NamedDB(cpName) localTx { implicit session =>

        val date = new DateTime().getMillis

        request.contextData.foreach { ctxPair =>
          val insert =
            sql"""
                INSERT INTO context_history (user_id, advertising_id, vendor_id, context_group, context_name, context_started_at_unix_time) VALUES
                (${request.userId}, ${request.idfa}, ${request.vendorId}, ${ctxPair.ctxGroup}, ${ctxPair.ctxName}, $date)
        """
          insert.updateAndReturnGeneratedKey().apply()
        }
      }
    } catch {
      case e: Exception =>
        println(e)
    }
  }

}
