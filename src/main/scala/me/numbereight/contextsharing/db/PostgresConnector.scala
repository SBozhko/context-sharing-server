package me.numbereight.contextsharing.db

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource
import me.numbereight.contextsharing.config.PostgresConfigParams
import org.slf4j.LoggerFactory
import scalikejdbc.ConnectionPool
import scalikejdbc.DataSourceConnectionPool
import scalikejdbc.NamedDB
import scalikejdbc.scalikejdbcSQLInterpolationImplicitDef

object PostgresConnector {
  val log = LoggerFactory.getLogger(getClass)

  Class.forName("org.postgresql.Driver")

  def createConnectionPool(name: String, connectionParams: PostgresConfigParams) = {
    val dataSource: DataSource = {
      val ds = new HikariDataSource()
      ds.setConnectionTimeout(connectionParams.connectionTimeout.toMillis)
      ds.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource")
      ds.addDataSourceProperty("url", connectionParams.url)
      ds.addDataSourceProperty("user", connectionParams.user)
      ds.addDataSourceProperty("password", connectionParams.password)
      ds.setMinimumIdle(10)
      ds.setMaximumPoolSize(10)
      ds.setPoolName(name)
      ds
    }
    ConnectionPool.add(name, new DataSourceConnectionPool(dataSource))
  }

  def isAlive(cpName: String): Boolean = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        sql"""
              SELECT 1
          """.execute.apply()
        true
      }
    } catch {
      case e: Exception =>
        log.error("Unable to check if DB is alive", e)
        false
    }
  }

  def initDb(cpName: String) = {
    try {
      NamedDB(cpName) localTx { implicit session =>
        sql"""
              CREATE TABLE IF NOT EXISTS context_history (
                id serial primary key,
                user_profile_id_id integer,
                context_group varchar(20),
                context_name varchar(20),
                context_started_at_unix_time bigint
              );
              CREATE TABLE IF NOT EXISTS place_history (
                id serial primary key,
                user_profile_id_id integer,
                latitude double precision,
                longitude double precision,
                place varchar(20),
                check_in_at_unix_time bigint
              );
              CREATE TABLE IF NOT EXISTS user_profiles (
                id serial primary key,
                user_id varchar(50),
                vendor_id varchar(50),
                advertising_id varchar(50) UNIQUE,
                timezone_offset_minutes smallint
              )
          """.execute.apply()
      }
    } catch {
      case e: Exception =>
        log.error("Unable to create tables", e)
    }
  }


}
