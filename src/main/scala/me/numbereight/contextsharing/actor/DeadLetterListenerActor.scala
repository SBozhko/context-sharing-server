package me.numbereight.contextsharing.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.DeadLetter
import akka.actor.Props
import akka.io.Tcp.Close
import akka.io.Tcp.ConfirmedClose
import spray.io.TickGenerator.Tick

class DeadLetterListenerActor extends Actor with ActorLogging {

  def receive = {
    case d@DeadLetter(ConfirmedClose, _, _) => log.debug("Dead letter received for connection confirmed close event: {}", d.toString)
    case d@DeadLetter(Close, _, _) => log.debug("Dead letter received for connection close event: {}", d.toString)
    case d@DeadLetter(Tick, _, _) => log.debug("Dead letter received for tick event: {}", d.toString)
    case d: DeadLetter => log.warning("Dead letter received: {}", d.toString)
  }

}

object DeadLetterListenerActor {
  val Name = "deadLetterListenerActor"

  def props(): Props = {
    Props.create(classOf[DeadLetterListenerActor])
  }
}