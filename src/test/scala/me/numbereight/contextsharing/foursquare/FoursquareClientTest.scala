package me.numbereight.contextsharing.foursquare

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import play.api.libs.ws.ahc.AhcWSClient

object FoursquareClientTest {

  def main(args: Array[String]) {

    implicit val system = ActorSystem("contextSharing")
    implicit val materializer =  ActorMaterializer()
    val httpClient = new AhcWSClient(new DefaultAsyncHttpClientConfig.Builder().build())
    val fsClient = new FoursquareClient(httpClient)

    //println(fsClient.performLocationCategoryQuery(LatLon("53.923464,27.481152")))
  }


}
