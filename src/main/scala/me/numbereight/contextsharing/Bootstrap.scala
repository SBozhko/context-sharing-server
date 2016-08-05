package me.numbereight.contextsharing

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.DeadLetter
import akka.io.IO
import me.numbereight.contextsharing.actor.ContextStorageActor
import me.numbereight.contextsharing.actor.DeadLetterListenerActor
import me.numbereight.contextsharing.actor.IsAliveServiceActor
import me.numbereight.contextsharing.actor.LifecycleListenerActor
import me.numbereight.contextsharing.actor.PlaceActor
import me.numbereight.contextsharing.actor.RecommendationsActor
import me.numbereight.contextsharing.actor.RestEndpointActor
import me.numbereight.contextsharing.actor.UserProfileActor
import me.numbereight.contextsharing.actor.UserStatsActor
import me.numbereight.contextsharing.config.RuntimeConfiguration
import me.numbereight.contextsharing.db.PostgresConnector
import me.numbereight.contextsharing.db.PostgresContextHistoryClient
import me.numbereight.contextsharing.db.PostgresPlaceHistoryClient
import me.numbereight.contextsharing.db.PostgresUserProfileClient
import me.numbereight.contextsharing.foursquare.FoursquareClient
import me.numbereight.contextsharing.http.ApplicationInfoHttpService
import me.numbereight.contextsharing.http.ContextStorageHttpService
import me.numbereight.contextsharing.http.PlaceHttpService
import me.numbereight.contextsharing.http.RecommendationsHttpService
import me.numbereight.contextsharing.http.UserProfileHttpService
import me.numbereight.contextsharing.http.UserStatsHttpService
import me.numbereight.contextsharing.soundcloud.SoundCloudClient
import org.slf4j.LoggerFactory
import spray.can.Http
import spray.can.Http.Bind

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object Bootstrap {

  def main(args: Array[String]) {

    val log = LoggerFactory.getLogger(Bootstrap.getClass)
    val runtimeConfig = RuntimeConfiguration
    val restConfig = runtimeConfig.restEndpointConfig

    val pgPoolName = "postgres"
    PostgresConnector.createConnectionPool(pgPoolName, runtimeConfig.PostgresConfig)
    PostgresConnector.initDb(pgPoolName)
    val pgContextClient = new PostgresContextHistoryClient(pgPoolName)
    val pgPlaceClient = new PostgresPlaceHistoryClient(pgPoolName)
    val pgUserProfileClient = new PostgresUserProfileClient(pgPoolName)

    implicit val system = ActorSystem("contextSharing")

    val deadLetterListener = system.actorOf(
      DeadLetterListenerActor.props(),
      DeadLetterListenerActor.Name)
    system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])


    val isAliveActor = system.actorOf(IsAliveServiceActor.props(pgPoolName))
    val applicationInfoHttpService = ApplicationInfoHttpService(system, isAliveActor)

    val ctxStorageActor = system.actorOf(ContextStorageActor.props(pgContextClient))
    val ctxStorageHttpService = ContextStorageHttpService(system, ctxStorageActor)

    val userStatsActor = system.actorOf(UserStatsActor.props(pgContextClient, pgUserProfileClient))
    val userStatsHttpService = UserStatsHttpService(system, userStatsActor)

    val placeActor = system.actorOf(PlaceActor.props(new FoursquareClient(), pgPlaceClient))
    val placeHttpService = PlaceHttpService(system, placeActor)

    val userProfileActor = system.actorOf(UserProfileActor.props(pgUserProfileClient))
    val userProfileHttpService = UserProfileHttpService(system, userProfileActor)

    val soundCloudClient = new SoundCloudClient()
    val recommendationsActor = system.actorOf(RecommendationsActor.props(soundCloudClient))
    val recommendationsHttpService = RecommendationsHttpService(system, recommendationsActor, ctxStorageActor)

    system.scheduler.schedule(
      new FiniteDuration(3, TimeUnit.SECONDS),
      new FiniteDuration(6, TimeUnit.HOURS),
      new Runnable {
        override def run(): Unit = {
          soundCloudClient.populateTracks()
        }
      })(ExecutionContext.Implicits.global)

    val restEndpointActor = system.actorOf(
      RestEndpointActor.props(
        applicationInfoHttpService,
        ctxStorageHttpService,
        userStatsHttpService,
        placeHttpService,
        userProfileHttpService,
        recommendationsHttpService
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
