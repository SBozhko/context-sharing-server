package me.numbereight.contextsharing.soundcloud

import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory

class SoundCloudClient {
  implicit val formats = DefaultFormats
  val log = LoggerFactory.getLogger(getClass)


}