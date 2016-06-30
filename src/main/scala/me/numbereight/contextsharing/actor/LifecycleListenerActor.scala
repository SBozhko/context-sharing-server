package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.io.Tcp
import akka.io.Tcp.Bound
import spray.can.Http

class LifecycleListenerActor extends Actor with ActorLogging {

  var listener: ActorRef = null

  override def receive: Receive = {
    case b@Bound(connection) =>
      log.info(b.toString)
      listener = sender()
      context.watch(listener)

    case Tcp.CommandFailed(bindMsg: Http.Bind) =>
      log.error("Busy endpoint: {}. Application is shutting down...", bindMsg.endpoint)
      context.system.terminate()
  }
}

object LifecycleListenerActor {
  val Name = "lifecycleListenerActor"

  def props: Props = {
    Props.create(classOf[LifecycleListenerActor])
  }
}
