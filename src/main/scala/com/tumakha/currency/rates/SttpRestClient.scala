package com.tumakha.currency.rates

import java.nio.charset.StandardCharsets.UTF_8

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.softwaremill.sttp.SttpBackendOptions.connectionTimeout
import com.softwaremill.sttp._
import com.softwaremill.sttp.akkahttp._
import com.softwaremill.sttp.json4s._
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Extraction}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language._

/**
 * @author Yuriy Tumakha
 */
class SttpRestClient(log: LoggingAdapter)(implicit system: ActorSystem) {

  implicit private val serialization = org.json4s.jackson.Serialization
  implicit private val jsonDefaultFormats = DefaultFormats

  implicit val backend: SttpBackend[Future, Source[ByteString, Any]] = AkkaHttpBackend.usingActorSystem(
    actorSystem = system,
    options = connectionTimeout(4 seconds))

  val jsonType = "application/json;charset=UTF-8"

  def getJson[T](url: String, headers: Map[String, String] = Map.empty)(implicit m: Manifest[T]): Future[Response[T]] = {
    log.info(s"GET $url")
    sttp.method(Method.GET, uri"$url").headers(headers).response(asJson[T]).send()
  }

  def postForJson[I, O](url: String, requestBody: I, headers: Map[String, String] = Map.empty)(implicit i: Manifest[I], o: Manifest[O]): Future[Response[O]] = {

    implicit val jsonBodySerializer: BodySerializer[I] = { body: I =>
      val serialized: String = JsonMethods.mapper.writeValueAsString(Extraction.decompose(body))
      log.info(serialized)
      StringBody(serialized, UTF_8.displayName, Some(jsonType))
    }

    sttp.method(Method.POST, uri"$url").headers(headers).body(requestBody).response(asJson[O]).send()
  }

}
