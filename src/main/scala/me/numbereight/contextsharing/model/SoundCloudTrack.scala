package me.numbereight.contextsharing.model

case class SoundCloudTrack(
  id: Long,
  duration: Int,
  permalinkUrl: String,
  streamUrl: String,
  artworkUrl: String,
  title: String,
  username: String)