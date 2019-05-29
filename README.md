# currency-converter #

Currency Converter - test task

Create a REST application with a single endpoint:

    POST /api/convert
    Body:
    {
        "fromCurrency": "GBP",
        "toCurrency" : "EUR",
        "amount" : 102.
    }

The return should be an object with the exchange rate between the "fromCurrency" to "toCurrency" 
and the amount converted to the second curency.

    {
        "exchange" : 1.11,
        "amount" : 113.886,
        "original" : 102.
    }

The exchange rates should be loaded from https://exchangeratesapi.io 
and assume the currency rates change every 1 minute.

## REST API curl example ##

Convert money **amount**

    curl -v -X POST --header "Content-Type: application/json" -d '{"fromCurrency": "GBP", "toCurrency" : "EUR", "amount" : 102.6}' localhost:8888/api/convert

## Requirements ##

* SBT 1.2+
* Scala 2.12+
* Java 8

## Build ##

    sbt assembly

## Run ##

    java -jar ./target/scala-2.12/currency-converter.jar