package com.tumakha.currency.rates

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, NoLogging}
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import akka.util.ByteString
import com.softwaremill.sttp.FutureMonad
import com.softwaremill.sttp.Method.GET
import com.softwaremill.sttp.testing.SttpBackendStub
import com.tumakha.currency.actor.Currency.{EUR, GBP, USD}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language._

/**
  * @author Yuriy Tumakha
  */
class ExchangeRatesAPISpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  val log: LoggingAdapter = NoLogging

  type BackendStub = SttpBackendStub[Future, Source[ByteString, Any]]

  "ExchangeRatesAPI" should "return latestRate for conversion fromCurrency => toCurrency" in {
    val gbpRatesResponse = Rates(GBP.toString, Map(EUR.toString -> 1.1, USD.toString -> 1.3))
    val ratesAPI = exchangeRatesAPI(_.whenRequestMatches(_.method == GET).thenRespond(gbpRatesResponse))

    Await.result(ratesAPI.latestRate(GBP, EUR), 3 seconds) shouldBe gbpRatesResponse.rates(EUR.toString)
  }

  it should "return latestRate even if external API response contains new unknown currency" in {
    val gbpRatesResponse = Rates(GBP.toString, Map(EUR.toString -> 1.1, "NNN" -> 1.3))
    val ratesAPI = exchangeRatesAPI(_.whenRequestMatches(_.method == GET).thenRespond(gbpRatesResponse))

    Await.result(ratesAPI.latestRate(GBP, EUR), 3 seconds) shouldBe gbpRatesResponse.rates(EUR.toString)
  }

  it should "return IllegalStateException if external API doesn't return rate for toCurrency" in {
    val gbpRatesResponse = Rates(GBP.toString, Map(USD.toString -> 1.3))
    val ratesAPI = exchangeRatesAPI(_.whenRequestMatches(_.method == GET).thenRespond(gbpRatesResponse))

    the [IllegalStateException] thrownBy
      Await.result(ratesAPI.latestRate(GBP, EUR), 3 seconds) should have message "Unknown rate for currency EUR"
  }

  it should "return IOException if SttpRestClient return Internal server error" in {
    val ratesAPI = exchangeRatesAPI(_.whenRequestMatches(_.method == GET).thenRespondServerError())

    the [IOException] thrownBy
      Await.result(ratesAPI.latestRate(GBP, EUR), 3 seconds) should have message "Internal server error"
  }

  private def exchangeRatesAPI(stubConfig: BackendStub => BackendStub): ExchangeRatesAPI =
    new ExchangeRatesAPI(log) {
      override val restClient: SttpRestClient = new SttpRestClient(log) {
        override val backend: BackendStub = stubConfig(SttpBackendStub(new FutureMonad()))
      }
    }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(actorSystem)

}
