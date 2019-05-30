package com.tumakha.currency.rates

import java.io.IOException

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.tumakha.currency.actor.Currency
import com.tumakha.currency.actor.Currency.Currency
import com.tumakha.currency.cache.RateCache

import scala.concurrent.{ ExecutionContext, Future }

/**
 * @author Yuriy Tumakha
 */

case class Rates(base: String, rates: Map[String, Double])

class ExchangeRatesAPI(log: LoggingAdapter)(implicit system: ActorSystem, ec: ExecutionContext) {

  val ratesBaseUrl = "https://api.exchangeratesapi.io"

  val restClient: SttpRestClient = new SttpRestClient(log)
  val rateCache: RateCache = new RateCache

  private def latestRatesUrl(baseCurrency: Currency): String = s"$ratesBaseUrl/latest?base=$baseCurrency"

  private val loadRates: Currency => Future[Map[Currency, Double]] = baseCurrency =>
    restClient.getJson[Rates](latestRatesUrl(baseCurrency)).map(_.body).map {
      case Left(error) => throw new IOException(error)
      case Right(result) =>
        result.rates collect { case (key, value) if Currency.contains(key) => Currency.withName(key) -> value }
    }

  def latestRate(from: Currency, to: Currency): Future[Double] =
    rateCache.cachedRates(from, loadRates)
      .map(rates => rates.getOrElse(to, throw new IllegalStateException(s"Unknown rate for currency $to")))

}
