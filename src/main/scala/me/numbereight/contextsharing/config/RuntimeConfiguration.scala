package me.numbereight.contextsharing.config

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

case class RestEndpointConfig(host: String, port: Int)

object RuntimeConfiguration extends StrictLogging {

  private val config = ConfigFactory.load

  val restEndpointConfig = {
    val host = config.getString("rest-endpoint.host")
    val port = config.getInt("rest-endpoint.port")
    RestEndpointConfig(host, port)
  }

}
