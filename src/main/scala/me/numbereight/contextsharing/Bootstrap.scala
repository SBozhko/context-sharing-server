package me.numbereight.contextsharing

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.DeadLetter
import akka.io.IO
import me.numbereight.contextsharing.actor.ContextStorageActor
import me.numbereight.contextsharing.actor.DeadLetterListenerActor
import me.numbereight.contextsharing.actor.IsAliveServiceActor
import me.numbereight.contextsharing.actor.LifecycleListenerActor
import me.numbereight.contextsharing.actor.RestEndpointActor
import me.numbereight.contextsharing.config.RuntimeConfiguration
import me.numbereight.contextsharing.db.DynamoDbClient
import me.numbereight.contextsharing.http.ApplicationInfoHttpService
import me.numbereight.contextsharing.http.ContextStorageHttpService
import org.slf4j.LoggerFactory
import spray.can.Http
import spray.can.Http.Bind

object Bootstrap {

  def main(args: Array[String]) {

    val log = LoggerFactory.getLogger(Bootstrap.getClass)
    val runtimeConfig = RuntimeConfiguration
    val restConfig = runtimeConfig.restEndpointConfig

    val system = ActorSystem("contextSharing")

    val deadLetterListener = system.actorOf(
      DeadLetterListenerActor.props(),
      DeadLetterListenerActor.Name)
    system.eventStream.subscribe(deadLetterListener, classOf[DeadLetter])

    val dynamoClient = new DynamoDbClient

    val isAliveActor = system.actorOf(IsAliveServiceActor.props(dynamoClient))
    val applicationInfoHttpService = ApplicationInfoHttpService(system, isAliveActor)

    val ctxStorageActor = system.actorOf(ContextStorageActor.props(dynamoClient))
    val ctxStorageHttpService = ContextStorageHttpService(system, ctxStorageActor)

    val restEndpointActor = system.actorOf(
      RestEndpointActor.props(applicationInfoHttpService, ctxStorageHttpService),
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
