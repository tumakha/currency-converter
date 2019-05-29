package com.tumakha.currency.cache

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.tumakha.currency.actor.Currency.{ Currency, EUR, GBP, USD }
import org.scalatest.{ BeforeAndAfterAll, FlatSpec, Matchers }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language._

/**
 * @author Yuriy Tumakha
 */
class RateCacheSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  implicit val actorSystem: ActorSystem = ActorSystem()

  import actorSystem.dispatcher

  val ratesMap: Map[Currency, Double] = Map(EUR -> 1.1, USD -> 1.3)
  val timeToLive: Duration = 3 seconds

  "RateCache" should "load uncached values with a given function" in {

    val cache = rateCache()

    Await.result(cache.cachedRates(EUR, _ => Future { ratesMap }), 3 seconds) shouldBe ratesMap
  }

  it should "cache values for timeToLive = 3 seconds" in {

    val cache = rateCache()
    val newMap = Map(EUR -> 1.2)

    Await.result(cache.cachedRates(GBP, _ => Future.successful(ratesMap)), 3 seconds) shouldBe ratesMap
    Await.result(cache.cachedRates(GBP, _ => Future.successful(newMap)), 3 seconds) shouldBe ratesMap
    Await.result(cache.cachedRates(GBP, _ => Future.successful(newMap)), 3 seconds) shouldBe ratesMap

    TimeUnit.SECONDS.sleep(3)
    Await.result(cache.cachedRates(GBP, _ => Future.successful(newMap)), 3 seconds) shouldBe newMap
  }

  def rateCache(): RateCache = new RateCache {
    override def cacheTimeToLive: Duration = timeToLive
  }

  override def afterAll(): Unit = TestKit.shutdownActorSystem(actorSystem)

}