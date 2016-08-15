package me.numbereight.contextsharing.util

import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.json4s.JValue
import org.json4s.JsonAST.JBool
import org.json4s.JsonAST.JInt
import org.json4s.JsonAST.JString

object HttpClientUtils {

  def queryParams2String(params: Map[String, Any]): String = {
    params.map {
      case (key, value) => s"$key=$value"
    }.mkString("&")
  }

  def params2Form(params: Map[String, Any]): Seq[NameValuePair] = {
    params.map {
      case (key: String, value: Any) => new BasicNameValuePair(key, value.toString)
    }.toSeq
  }

  def parseAsLong(jValue: JValue): Long = {
    jValue match {
      case JInt(value) => value.longValue()
      case unknown => throw new IllegalArgumentException(s"Unexpected value for json field: $unknown")
    }
  }

  def parseAsBoolean(jValue: JValue): Boolean = {
    jValue match {
      case JBool(value) => value
      case unknown => throw new IllegalArgumentException(s"Unexpected value for json field: $unknown")
    }
  }

  def parseAsString(jValue: JValue): String = {
    jValue match {
      case JString(value) => value
      case unknown => throw new IllegalArgumentException(s"Unexpected value for json field: $unknown")
    }
  }

}
