package me.numbereight.contextsharing.config

import java.time.Duration

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

case class RestEndpointConfig(host: String, port: Int)
case class PostgresConfigParams(url: String, user: String, password: String, connectionTimeout: Duration)

object RuntimeConfiguration extends StrictLogging {

  private val config = ConfigFactory.load

  val restEndpointConfig = {
    val host = config.getString("rest-endpoint.host")
    val port = config.getInt("rest-endpoint.port")
    RestEndpointConfig(host, port)
  }
  val PostgresConfig = {
    val user = config.getString("postgres.user")
    val password = config.getString("postgres.password")
    val url = config.getString("postgres.url")
    val timeout = config.getDuration("postgres.connection-timeout")
    PostgresConfigParams(url, user, password, timeout)
  }


}
