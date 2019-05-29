package com.tumakha.currency

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.tumakha.currency.actor.MoneyExchangeActor

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Top level application class to launch the Currency Converter REST service.
 *
 * @author Yuriy Tumakha
 */
object HttpServer extends App with ApiRoutes {

  val host = "localhost"
  val port = 8888

  implicit val system: ActorSystem = ActorSystem("currencyConverterServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val moneyExchangeActor: ActorRef = system.actorOf(MoneyExchangeActor.props, "moneyExchangeActor")

  lazy val routes: Route = apiRoutes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Http server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)

}
