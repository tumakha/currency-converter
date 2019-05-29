package com.tumakha.currency.cache

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{ Cache, CachingSettings }
import com.tumakha.currency.actor.Currency
import com.tumakha.currency.actor.Currency.Currency

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language._

/**
 * @author Yuriy Tumakha
 */
class RateCache(implicit system: ActorSystem) {

  def cacheTimeToLive: Duration = 1 minute

  type RatesMap = Map[Currency, Double]

  private val cache: Cache[Currency, RatesMap] = lfuCache(
    initialCapacity = Currency.values.size,
    timeToLive = cacheTimeToLive)

  private def lfuCache[K, V](maxCapacity: Int = 500, initialCapacity: Int = 16,
    timeToLive: Duration = Duration.Inf, timeToIdle: Duration = Duration.Inf): Cache[K, V] = {
    LfuCache[K, V] {
      val settings = CachingSettings(system)
      settings.withLfuCacheSettings(
        settings.lfuCacheSettings
          .withMaxCapacity(maxCapacity)
          .withInitialCapacity(initialCapacity)
          .withTimeToLive(timeToLive)
          .withTimeToIdle(timeToIdle))
    }
  }

  def cachedRates(baseCurrency: Currency, load: Currency => Future[RatesMap]): Future[RatesMap] =
    cache.getOrLoad(baseCurrency, load)

}
