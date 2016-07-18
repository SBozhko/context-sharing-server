package me.numbereight.contextsharing

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.DeadLetter
import akka.io.IO
import akka.stream.ActorMaterializer
import me.numbereight.contextsharing.actor.ContextStorageActor
import me.numbereight.contextsharing.actor.DeadLetterListenerActor
import me.numbereight.contextsharing.actor.IsAliveServiceActor
import me.numbereight.contextsharing.actor.LifecycleListenerActor
import me.numbereight.contextsharing.actor.PlaceActor
import me.numbereight.contextsharing.actor.RestEndpointActor
import me.numbereight.contextsharing.actor.UserStatsActor
import me.numbereight.contextsharing.config.RuntimeConfiguration
import me.numbereight.contextsharing.db.PostgresConnector
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.foursquare.FoursquareClient
import me.numbereight.contextsharing.http.ApplicationInfoHttpService
import me.numbereight.contextsharing.http.ContextStorageHttpService
import me.numbereight.contextsharing.http.PlaceHttpService
import me.numbereight.contextsharing.http.UserStatsHttpService
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.slf4j.LoggerFactory
import play.api.libs.ws.ahc.AhcWSClient
import spray.can.Http
import spray.can.Http.Bind

object Bootstrap {

  def main(args: Array[String]) {

    val log = LoggerFactory.getLogger(Bootstrap.getClass)
    val runtimeConfig = RuntimeConfiguration
    val restConfig = runtimeConfig.restEndpointConfig

    val pgPoolName = "postgres"
    PostgresConnector.createConnectionPool(pgPoolName, runtimeConfig.PostgresConfig)
    val pgClient = new PostgresContextHistoryClient(pgPoolName)
    pgClient.initDb()

    implicit val system = ActorSystem("contextSharing")

    val deadLetterListener = system.actorOf(
      DeadLetterListenerActor.props(),
      DeadLetterListenerActor.Name)
    system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])

    val httpClient = new AhcWSClient(new DefaultAsyncHttpClientConfig.Builder().build())(ActorMaterializer())
    val fsClient = new FoursquareClient(httpClient)


    val isAliveActor = system.actorOf(IsAliveServiceActor.props(pgClient))
    val applicationInfoHttpService = ApplicationInfoHttpService(system, isAliveActor)

    val ctxStorageActor = system.actorOf(ContextStorageActor.props(pgClient))
    val ctxStorageHttpService = ContextStorageHttpService(system, ctxStorageActor)

    val userStatsActor = system.actorOf(UserStatsActor.props(pgClient))
    val userStatsHttpService = UserStatsHttpService(system, userStatsActor)

    val placeActor = system.actorOf(PlaceActor.props(fsClient))
    val placeHttpService = PlaceHttpService(system, placeActor)

    val restEndpointActor = system.actorOf(
      RestEndpointActor.props(
        applicationInfoHttpService,
        ctxStorageHttpService,
        userStatsHttpService,
        placeHttpService
      ),
      RestEndpointActor.Name)


    val bindMessage: Bind = Http.Bind(restEndpointActor, restConfig.host, restConfig.port)
    val ioActor: ActorRef = IO(Http)(system)
    val lifecycleListener = system.actorOf(LifecycleListenerActor.props)
    ioActor.tell(bindMessage, lifecycleListener)

    sys.addShutdownHook {
      log.info("Shutting down...")
    }

  }
}
