package me.numbereight.contextsharing.model

import me.numbereight.contextsharing.foursquare.LatLong
import me.numbereight.contextsharing.util.BuildInfoProvider
import me.numbereight.contextsharing.util.GitInfoProvider
import spray.routing.RequestContext

case class Response(messages: String*)

case class IsAliveResponse(apiIsAlive: Boolean, databaseIsAlive: Option[Boolean] = None)

case class VersionResponse(
  commitId: String,
  commitUser: String,
  commitTime: String,
  buildTime: String,
  branchName: String,
  finalName: String,
  artifactId: String,
  groupId: String,
  version: String
)


object VersionResponse {

  def load: VersionResponse = {
    VersionResponse(
      commitId = GitInfoProvider.commitId,
      commitUser = GitInfoProvider.commitUser,
      commitTime = GitInfoProvider.commitTime,
      buildTime = GitInfoProvider.buildTime,
      branchName = GitInfoProvider.branchName,
      finalName = BuildInfoProvider.finalName,
      artifactId = BuildInfoProvider.artifactId,
      groupId = BuildInfoProvider.groupId,
      version = BuildInfoProvider.version
    )
  }
}


case class SubmitContextRequest(profileId: Long, contextData: List[ContextData])
case class SubmitContextResponse(contextData: List[ContextDataPair])

case class ContextData(ctxGroup: String, ctxName: String, manual: Boolean = false)
case class ContextDataPair(ctxGroup: String, ctxName: String)
case class ContextManuallyUpdated(name: String, updatedAt: Long)

case class GetUserStatsRequest(profileId: Long, ctxGroups: List[String], period: String)
case class GetUserStatsActorRequest(sprayCtx: RequestContext, request: GetUserStatsRequest)
case class GetUserStatsResponse(userStats: List[CtxStats])
case class CtxStats(ctxGroup: String, values: List[CtxPercentage])
case class CtxPercentage(ctxName: String, percentage: Double)
object StatsPeriod {
  val Day = "day"
  val Week = "week"
  val Month = "month"
  val All = Set(Day, Week, Month)
  def valid(period: String): Boolean = All.contains(period)
}

case class GetPlace(sprayCtx: RequestContext, latLong: LatLong, vendorId: Option[String])
case class PlaceResponse(place: String)
case class PlaceEvent(latLong: LatLong, vendorId: String, place: String)

case class SubmitUserInfoRequest(userId: String, vendorId: String, advertisingId: String, timezone: String, name: Option[String])
case class SubmitUserInfoMessage(userId: String, vendorId: String, advertisingId: String, timezoneOffsetMins: Int, name: Option[String])
case class SubmitUserProfile(sprayCtx: RequestContext, request: SubmitUserInfoMessage)
case class UserProfileResponse(profileId: Long)

case class RecommendationsResponse(
  order: List[OrderItem],
  musicItems: List[MusicItem],
  videoItems: List[VideoItem])
case class MusicItem(id: Long, title: String, artist: String, url: String, streamUrl: String, duration: Int, artwork: String)
case class VideoItem(id: Long, title: String, url: String, embedUrl: String, duration: Int, artwork: String)
case class OrderItem(`type`: String, id: Long)

case class SubmitPlaceRequest(vendorId: String, placeName: String, lat: Double, long: Double)