package com.tumakha.currency

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.tumakha.currency.actor.Currency.Currency
import com.tumakha.currency.actor.{ ConversionResult, ConvertMoney, Currency, Error }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, JsonFormat, deserializationError }

import scala.util.Try

/**
 * @author Yuriy Tumakha
 */
trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val currencyJsonFormat = new JsonFormat[Currency] {
    override def read(json: JsValue): Currency = json match {
      case JsString(currency) =>
        Try(Currency.withName(currency)).getOrElse(deserializationError(s"Not supported currency $currency"))
      case somethingElse => deserializationError(s"Currency should be JSON string instead of $somethingElse")
    }

    override def write(obj: Currency): JsValue = JsString(obj.toString)
  }

  implicit val convertMoneyJsonFormat = jsonFormat3(ConvertMoney)
  implicit val conversionResultJsonFormat = jsonFormat3(ConversionResult)
  implicit val errorJsonFormat = jsonFormat1(Error)

}
