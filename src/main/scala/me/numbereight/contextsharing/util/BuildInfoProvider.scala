package me.numbereight.contextsharing.util

import com.typesafe.config.ConfigFactory

import scala.util.Try

object BuildInfoProvider {

  val finalName = getFieldValue("build.final.name")
  val artifactId = getFieldValue("artifact.id")
  val groupId = getFieldValue("group.id")
  val version = getFieldValue("version")
  private val BuildInfoFile = "build-info.properties"
  private val buildInfoConfig = ConfigFactory.load(BuildInfoFile)

  private def getFieldValue(field: String): String = {
    Try(buildInfoConfig.getString(field)).getOrElse("N/A")
  }

}
