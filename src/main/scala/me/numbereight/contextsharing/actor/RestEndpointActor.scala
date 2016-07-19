package me.numbereight.contextsharing.actor

import akka.actor.Props
import me.numbereight.contextsharing.http.ApplicationInfoHttpService
import me.numbereight.contextsharing.http.ContextStorageHttpService
import me.numbereight.contextsharing.http.PlaceHttpService
import me.numbereight.contextsharing.http.UserProfileHttpService
import me.numbereight.contextsharing.http.UserStatsHttpService

class RestEndpointActor(
  applicationInfoService: ApplicationInfoHttpService,
  ctxStorageHttpService: ContextStorageHttpService,
  userStatsHttpService: UserStatsHttpService,
  placeHttpService: PlaceHttpService,
  userProfileHttpService: UserProfileHttpService)
  extends BaseHttpServiceActor {

  def receive = runRoute(
    applicationInfoService.routes ~
    ctxStorageHttpService.routes ~
    userStatsHttpService.routes ~
    placeHttpService.routes ~
    userProfileHttpService.routes)
}

object RestEndpointActor {

  val Name = "restEndpointActor"

  def props(
    applicationInfoService: ApplicationInfoHttpService,
    ctxStorageHttpService: ContextStorageHttpService,
    userStatsHttpService: UserStatsHttpService,
    placeHttpService: PlaceHttpService,
    userProfileHttpService: UserProfileHttpService): Props = {

    Props.create(
      classOf[RestEndpointActor],
      applicationInfoService,
      ctxStorageHttpService,
      userStatsHttpService,
      placeHttpService,
      userProfileHttpService
    )
  }
}