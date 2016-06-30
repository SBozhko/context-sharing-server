package me.numbereight.contextsharing.model

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
  version: String)


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


case class SubmitContextRequest(
  userId: String,
  vendorId: String,
  contextData: List[ContextData]
  )

case class SubmitContextActorRequest(sprayCtx: RequestContext, request: SubmitContextRequest)

case class ContextData(ctxType: String, ctxValue: String)
