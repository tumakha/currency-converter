package com.tumakha.currency

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.tumakha.currency.actor.Currency.{Currency, EUR, GBP}
import com.tumakha.currency.actor.{ConversionResult, ConvertMoney, Error, MoneyExchangeActor}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

/**
 * @author Yuriy Tumakha
 */
class ApiRoutesSpec extends FlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with ApiRoutes {

  val rate = 1.11
  val conversionResult = ConversionResult(rate, 113.886, 102.6)

  override val moneyExchangeActor: ActorRef = system.actorOf(Props(new MoneyExchangeActor() {
    override def preStart(): Unit = ()

    override def getExchangeRate(fromCurrency: Currency, toCurrency: Currency): Future[Double] = Future.successful(rate)
  }))

  implicit def rejectionHandler = apiRejectionHandler

  lazy val routes = apiRoutes

  "ApiRoutes (POST /api/convert)" should "return conversion result" in {

    val convertMoney = ConvertMoney(GBP, EUR, 102.6)
    val convertMoneyEntity = Marshal(convertMoney).to[MessageEntity].futureValue

    val request = Post("/api/convert").withEntity(convertMoneyEntity)

    request ~> routes ~> check {
      status shouldBe StatusCodes.OK

      contentType shouldBe `application/json`

      entityAs[ConversionResult] shouldBe conversionResult
    }
  }

  it should "return exception for not supported fromCurrency" in {
    val convertMoneyJson = """{"fromCurrency": "UAH", "toCurrency": "ZZZ", "amount": 102.6}"""
    val convertMoneyEntity = Marshal(convertMoneyJson).to[RequestEntity].futureValue

    val request = Post("/api/convert").withEntity(convertMoneyEntity.withContentType(`application/json`))

    request ~> Route.seal(routes) ~> check {
      status shouldBe StatusCodes.BadRequest

      contentType shouldBe `application/json`

      entityAs[Error] shouldBe Error("Not supported currency UAH")
    }
  }

  it should "return exception for not supported toCurrency" in {
    val convertMoneyJson = """{"fromCurrency": "GBP", "toCurrency": "ZzZ", "amount": 102.6}"""
    val convertMoneyEntity = Marshal(convertMoneyJson).to[MessageEntity].futureValue

    val request = Post("/api/convert").withEntity(convertMoneyEntity.withContentType(`application/json`))

    request ~> Route.seal(routes) ~> check {
      status shouldBe StatusCodes.BadRequest

      contentType shouldBe `application/json`

      entityAs[String] shouldBe """{"message":"Not supported currency ZzZ"}"""
    }
  }

}
