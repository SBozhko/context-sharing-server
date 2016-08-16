package me.numbereight.contextsharing.actor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.actor.Props
import com.google.common.cache.CacheBuilder
import me.numbereight.contextsharing.actor.ManualContextDataActor.OverriddenContextData
import me.numbereight.contextsharing.actor.ManualContextDataActor.StoreManualContextData
import me.numbereight.contextsharing.db.PostgresManualContextUpdatesClient
import me.numbereight.contextsharing.model.ContextData
import me.numbereight.contextsharing.model.ContextDataPair

case class CacheKey(profileId: Long, ctxGroup: String)

class ManualContextDataActor(pgClient: PostgresManualContextUpdatesClient) extends BaseHttpServiceActor {

  private val cache = CacheBuilder.newBuilder().expireAfterWrite(60l, TimeUnit.MINUTES).build[CacheKey, String]()

  override def receive: Actor.Receive = {
    case msg: StoreManualContextData =>
      // put manual data to db and cache
      val manualData = msg.ctxData
        .filter(item => item.manual)
        .map(item => ContextDataPair(item.ctxGroup, item.ctxName))
      pgClient.saveContextData(msg.profileId, manualData)

      manualData.foreach { item =>
        val key = CacheKey(msg.profileId, item.ctxGroup)
        cache.put(key, item.ctxName)
      }

      // get contexts for non-manual data from cache, then - from db
      val notManualData = msg.ctxData
        .filterNot(item => item.manual)
        .map { item =>
          val valueFromCache = cache.getIfPresent(CacheKey(msg.profileId, item.ctxGroup))
          if (valueFromCache == null) {
            pgClient.getManualContextData(msg.profileId, item.ctxGroup) match {
              case Some(name) => ContextDataPair(item.ctxGroup, name)
              case None => ContextDataPair(item.ctxGroup, item.ctxName)
            }
          } else {
            ContextDataPair(item.ctxGroup, valueFromCache)
          }
        }

      sender().tell(OverriddenContextData(manualData ++ notManualData), self)

    case something: Any =>
      handleUnknownMsg(something)
  }
}

object ManualContextDataActor {

  case class StoreManualContextData(profileId: Long, ctxData: List[ContextData])
  case class OverriddenContextData(ctxData: List[ContextDataPair])

  val Name = "manualContextDataActor"

  def props(pgClient: PostgresManualContextUpdatesClient): Props = {
    Props.create(classOf[ManualContextDataActor], pgClient)
  }

}