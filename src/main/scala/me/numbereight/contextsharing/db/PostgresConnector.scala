package me.numbereight.contextsharing.db

import javax.sql.DataSource

import com.zaxxer.hikari.HikariDataSource
import me.numbereight.contextsharing.config.PostgresConfigParams
import scalikejdbc.ConnectionPool
import scalikejdbc.DataSourceConnectionPool

object PostgresConnector {

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


}
