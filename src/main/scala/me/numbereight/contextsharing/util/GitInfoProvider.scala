package me.numbereight.contextsharing.util

import com.typesafe.config.ConfigFactory

import scala.util.Try

object GitInfoProvider {

  val commitId = getFieldValue("git.commit.id.describe")
  val commitUser = getFieldValue("git.commit.user.name")
  val commitTime = getFieldValue("git.commit.time")
  val buildTime = getFieldValue("git.build.time")
  val branchName = getFieldValue("git.branch")
  private val GitInfoFile = "git.properties"
  private val gitInfoConfig = ConfigFactory.load(GitInfoFile)

  private def getFieldValue(field: String): String = {
    Try(gitInfoConfig.getString(field)).getOrElse("N/A")
  }

}
