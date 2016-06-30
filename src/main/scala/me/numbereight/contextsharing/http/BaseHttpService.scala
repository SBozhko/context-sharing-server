package me.numbereight.contextsharing.http

import akka.actor.ActorSystem
import akka.event.Logging
import org.json4s.DefaultFormats
import spray.routing.HttpService

trait BaseHttpService extends HttpService {

  implicit val formats = DefaultFormats

  val log = Logging.getLogger(actorSystem, this)
  val ApiVersion = "v1"

  protected def actorSystem: ActorSystem

}