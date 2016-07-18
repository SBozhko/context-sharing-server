package me.numbereight.contextsharing.foursquare

case class VenueResponse(meta: Meta, response: Response)

case class Meta(code: Int, requestId: String)

case class Response(venues: List[Venue], confident: Boolean)

case class Venue(
  id: String,
  name: String,
  location: Location,
  categories: List[Category],
  verified: Boolean,
  allowMenuUrlEdit: Boolean,
  specials: Specials,
  hereNow: HereNow,
  referralId: String,
  venueChains: List[Object],
  hasPerk: Boolean
)

case class Location(lat: Double, lng: Double, distance: Int, cc: String, country: String, formattedAddress: List[String])

case class Category(id: String, name: String, pluralName: String, shortName: String, icon: Icon, primary: Boolean)

case class Icon(prefix: String, suffix: String)

case class Stats(checkinsCount: Int, usersCount: Int, tipCount: Int)

case class Specials(count: Int, items: List[Object])

case class HereNow(count: Int, summary: String, groups: List[Object])

