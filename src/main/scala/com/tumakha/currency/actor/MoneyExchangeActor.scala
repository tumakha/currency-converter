package com.tumakha.currency.actor

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.pattern.pipe
import com.tumakha.currency.actor.Currency.{ Currency, EUR, GBP }
import com.tumakha.currency.rates.ExchangeRatesAPI
import com.tumakha.currency.util.MathUtil.round

import scala.concurrent.{ ExecutionContext, Future }

/**
 * @author Yuriy Tumakha
 */
object Currency extends Enumeration {
  type Currency = Value
  val EUR, BGN, NZD, ILS, RUB, CAD, USD, PHP, CHF, ZAR, AUD, JPY, TRY, HKD, MYR, THB, HRK, NOK, IDR, DKK, CZK, HUF, GBP, MXN, KRW, ISK, SGD, BRL, PLN, INR, RON, CNY, SEK = Value

  def contains(name: String): Boolean = values.exists(_.toString == name)

}

final case class ConvertMoney(fromCurrency: Currency, toCurrency: Currency, amount: Double)

final case class ConversionResult(exchange: Double, amount: Double, original: Double)

final case class Error(message: String)

object MoneyExchangeActor {
  def props: Props = Props[MoneyExchangeActor]
}

class MoneyExchangeActor extends Actor with ActorLogging {

  implicit def system: ActorSystem = context.system

  implicit def ec: ExecutionContext = context.dispatcher

  val ratesApi = new ExchangeRatesAPI(log)

  override def preStart(): Unit = ratesApi.latestRate(EUR, GBP) // init sttp client

  def receive: Receive = {
    case convertMoney: ConvertMoney => convert(convertMoney) pipeTo sender()
    case msg => log.warning(s"Received unknown message: $msg")
  }

  def convert(convertMoney: ConvertMoney): Future[ConversionResult] =
    getExchangeRate(convertMoney.fromCurrency, convertMoney.toCurrency).map(exchange => {
      val original = round(convertMoney.amount)
      val amount = round(original * exchange)
      ConversionResult(exchange, amount, original)
    })

  def getExchangeRate(fromCurrency: Currency, toCurrency: Currency): Future[Double] =
    if (fromCurrency == toCurrency) Future.successful(1)
    else ratesApi.latestRate(fromCurrency, toCurrency)

}
