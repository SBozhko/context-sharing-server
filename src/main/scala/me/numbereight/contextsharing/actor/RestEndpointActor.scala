package me.numbereight.contextsharing.actor

import akka.actor.Props
import me.numbereight.contextsharing.http.ApplicationInfoHttpService

class RestEndpointActor(
  applicationInfoService: ApplicationInfoHttpService)
  extends BaseHttpServiceActor {

  def receive = runRoute(applicationInfoService.routes)
}

object RestEndpointActor {

  val Name = "restEndpointActor"

  def props(
    applicationInfoService: ApplicationInfoHttpService): Props = {

    Props.create(
      classOf[RestEndpointActor],
      applicationInfoService
    )
  }
}