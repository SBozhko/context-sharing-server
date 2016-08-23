package me.numbereight.contextsharing.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import me.numbereight.contextsharing.actor.ContextStorageActor.SubmitContextActorRequest
import me.numbereight.contextsharing.actor.ManualContextDataActor.OverriddenContextData
import me.numbereight.contextsharing.actor.ManualContextDataActor.StoreManualContextData
import me.numbereight.contextsharing.model.ContextData
import me.numbereight.contextsharing.model.ContextDataPair
import me.numbereight.contextsharing.model.ContextGroups
import me.numbereight.contextsharing.model.ContextNames
import me.numbereight.contextsharing.model.SubmitContextRequest
import org.json4s.jackson.Serialization
import spray.http.StatusCodes
import spray.routing.Route

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

trait ContextStorageHttpService extends BaseHttpService {

  val routes = storeContext()

  def storeContext(): Route = post {
    pathPrefix(ApiVersion) {
      path("contexts") { sprayCtx =>
        val req = Serialization.read[SubmitContextRequest](sprayCtx.request.entity.asString)
        validateRequest(req) match {
          case Right(list) =>

            implicit val timeout = Timeout(5, TimeUnit.SECONDS)
            val future = manualCtxUpdateActor.ask(StoreManualContextData(req.profileId, req.contextData))

            future.onComplete {
              case Success(overriddenData: OverriddenContextData) =>
                val actorRequest = SubmitContextActorRequest(sprayCtx, req.profileId, overriddenData.ctxData)
                ctxStorageActor.tell(actorRequest, ActorRef.noSender)
              case Failure(t) =>
                log.error(t, s"Unable to override context data with manual information for user with profile id: ${req.profileId}")
                val ctxData = list.map(item => ContextDataPair(item.ctxGroup, item.ctxName))
                val actorRequest = SubmitContextActorRequest(sprayCtx, req.profileId, ctxData)
                ctxStorageActor.tell(actorRequest, ActorRef.noSender)
            }
          case Left(reason) =>
            sprayCtx.complete(StatusCodes.BadRequest, reason)
        }
      }
    }
  }

  private def validateRequest(rq: SubmitContextRequest): Either[String, List[ContextData]] = {
    var valid: Either[String, List[ContextData]] = Right(rq.contextData)

    val contextDataValidated = rq.contextData.map { item =>
      if (ContextNames.valid(item.ctxGroup, item.ctxName)) {
        item
      } else {
        if (item.manual && ContextGroups.valid(item.ctxGroup)) {
          item.copy(ctxName = if (item.ctxName.length > 20) item.ctxName.substring(0, 20) else item.ctxName)
        } else {
          valid = Left(s"Wrong ContextName(s) and/or ContextGroup(s) $item")
          item
        }
      }
    }

    if (valid.isRight) {
      Right(contextDataValidated)
    } else {
      valid
    }
  }

  protected def ctxStorageActor: ActorRef

  protected def manualCtxUpdateActor: ActorRef
}

object ContextStorageHttpService {
  def apply(system: ActorSystem, storageActor: ActorRef, manualCtxUpdateActorRef: ActorRef): ContextStorageHttpService = {
    new ContextStorageHttpService {
      override implicit def actorRefFactory: ActorRefFactory = system

      override protected def actorSystem: ActorSystem = system

      override protected def ctxStorageActor: ActorRef = storageActor

      override protected def manualCtxUpdateActor: ActorRef = manualCtxUpdateActorRef
    }
  }
}