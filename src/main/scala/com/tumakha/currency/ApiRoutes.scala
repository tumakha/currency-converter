package com.tumakha.currency

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.{ StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ MalformedRequestContentRejection, MethodRejection, RejectionHandler, Route, StandardRoute, ValidationRejection }
import akka.pattern.ask
import akka.util.Timeout
import com.tumakha.currency.actor.{ ConversionResult, ConvertMoney, Error }

import scala.concurrent.duration._
import scala.language._
import scala.util.{ Failure, Success }

/**
 * @author Yuriy Tumakha
 */
trait ApiRoutes extends JsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[ApiRoutes])

  def moneyExchangeActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5 seconds)

  implicit def apiRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case ValidationRejection(msg, _) => errorRoute(StatusCodes.BadRequest, msg)
      }
      .handle {
        case MalformedRequestContentRejection(msg, _) => errorRoute(StatusCodes.BadRequest, msg)
      }
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        errorRoute(StatusCodes.BadRequest, s"Supported methods: ${names mkString " or "}")
      }
      .handleNotFound {
        errorRoute(StatusCodes.NotFound, "Not Found")
      }
      .result()

  lazy val apiRoutes: Route =
    pathPrefix("api") {
      pathPrefix("convert") {
        pathEnd {
          post {
            entity(as[ConvertMoney]) { convertMoney =>
              val conversionFuture = (moneyExchangeActor ? convertMoney).mapTo[ConversionResult]
              onComplete(conversionFuture) {
                case Success(result) => {
                  log.info("Convert {} => {}", convertMoney, result)
                  complete(result)
                }
                case Failure(ex) => {
                  log.error(ex, "Conversion failed")
                  errorRoute(StatusCodes.InternalServerError, s"${ex.getClass.getName}: ${ex.getMessage}")
                }
              }
            }
          }
        }
      }
    }

  private def errorRoute(statusCode: StatusCode, message: String): StandardRoute = complete(statusCode, Error(message))

}
