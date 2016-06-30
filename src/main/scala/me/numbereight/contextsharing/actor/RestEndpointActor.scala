package me.numbereight.contextsharing.actor

import akka.actor.Props
import me.numbereight.contextsharing.http.ApplicationInfoHttpService
import me.numbereight.contextsharing.http.ContextStorageHttpService

class RestEndpointActor(
  applicationInfoService: ApplicationInfoHttpService,
  ctxStorageHttpService: ContextStorageHttpService)
  extends BaseHttpServiceActor {

  def receive = runRoute(applicationInfoService.routes ~ ctxStorageHttpService.routes)
}

object RestEndpointActor {

  val Name = "restEndpointActor"

  def props(
    applicationInfoService: ApplicationInfoHttpService,
    ctxStorageHttpService: ContextStorageHttpService): Props = {

    Props.create(
      classOf[RestEndpointActor],
      applicationInfoService,
      ctxStorageHttpService
    )
  }
}