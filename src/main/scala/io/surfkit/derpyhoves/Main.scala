package io.surfkit.derpyhoves

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import io.surfkit.derpyhoves.flows._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

object Main extends App{

  val API_KEY = "XXX"

  override def main(args: Array[String]) {

    val decider: Supervision.Decider = {
      case _ => Supervision.Resume
    }
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))
/*
    import scala.concurrent.duration._
    val request: _root_.akka.http.scaladsl.model.HttpRequest = RequestBuilding.Get(Uri("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=PMGX9ASKF5L4PW7E"))
    val source: Source[HttpRequest, Cancellable] = Source.tick(0.seconds, 20.seconds, request)
    val sourceWithDest: Source[Try[HttpResponse], Cancellable] =
      source.map(req ⇒ (req, NotUsed)).via(Http().superPool[NotUsed]()).map(_._1)

    sourceWithDest.runForeach(i => println(i))(materializer)*/

    //val msft = AlphaVantageTimeSeries("MSFT", AlphaVantage.Interval.`1min`)
    //msft.json.runForeach(i => i.foreach(x => x.foreach(println) ) )(materializer)

    try {
      val json =
        """
          |{
          |	"success": true,
          |	"message": "",
          |	"result": [{
          |		"MarketCurrency": "LTC",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Litecoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-LTC",
          |		"IsActive": true,
          |		"Created": "2014-02-13T00:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/6defbc41-582d-47a6-bb2e-d0fa88663524.png"
          |	}, {
          |		"MarketCurrency": "DOGE",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Dogecoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-DOGE",
          |		"IsActive": true,
          |		"Created": "2014-02-13T00:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/a2b8eaee-2905-4478-a7a0-246f212c64c6.png"
          |	}, {
          |		"MarketCurrency": "VTC",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Vertcoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-VTC",
          |		"IsActive": true,
          |		"Created": "2014-02-13T00:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/1f0317bc-c44b-4ea4-8a89-b9a71f3349c8.png"
          |	}, {
          |		"MarketCurrency": "PPC",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Peercoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-PPC",
          |		"IsActive": true,
          |		"Created": "2014-02-13T00:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": null
          |	}, {
          |		"MarketCurrency": "FTC",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Feathercoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-FTC",
          |		"IsActive": true,
          |		"Created": "2014-02-13T00:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/a69f83f0-112c-4d98-8e14-5e0e9be47404.png"
          |	}, {
          |		"MarketCurrency": "RDD",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "ReddCoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-RDD",
          |		"IsActive": true,
          |		"Created": "2014-02-25T09:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/4b7ce5a5-8c12-4741-86f2-3a48cb55c91a.png"
          |	}, {
          |		"MarketCurrency": "NXT",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "NXT",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-NXT",
          |		"IsActive": true,
          |		"Created": "2014-03-03T09:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/443d492d-4f8b-4a2d-a613-1b37e4ab80cd.png"
          |	}, {
          |		"MarketCurrency": "DASH",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "Dash",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-DASH",
          |		"IsActive": true,
          |		"Created": "2014-03-11T08:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/49993d38-d344-4197-b449-c50c3cc13d47.png"
          |	}, {
          |		"MarketCurrency": "POT",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "PotCoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-POT",
          |		"IsActive": true,
          |		"Created": "2014-03-11T08:00:00",
          |		"Notice": null,
          |		"IsSponsored": null,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/149a1a49-3cca-461b-a8e4-8ea8409c27bd.png"
          |	}, {
          |		"MarketCurrency": "BLK",
          |		"BaseCurrency": "BTC",
          |		"MarketCurrencyLong": "BlackCoin",
          |		"BaseCurrencyLong": "Bitcoin",
          |		"MinTradeSize": 0.00000001,
          |		"MarketName": "BTC-BLK",
          |		"IsActive": true,
          |		"Created": "2014-03-14T09:00:00",
          |		"Notice": null,
          |		"IsSponsored": false,
          |		"LogoUrl": "https://bittrexblobstorage.blob.core.windows.net/public/c3409d42-a907-4764-ad03-118917761cc2.png"
          |	}
          |    ]
          |}
          |
        """.stripMargin

      val test = Json.parse(json).as[Bittrex.Response[Bittrex.Market]]
      println(s"test: ${test}")

      /*println("calling EMA")
      val fx = AlphaVantage.AlphaVantageEMA.get("MSFT", AlphaVantage.Interval.`1min`, 60)
      fx.foreach { x =>
        println(s"GOT IT: ")
      }*/
      /*println("calling MACD")
      val fx2 = AlphaVantage.AlphaVantageMACD.get("MSFT", AlphaVantage.Interval.`1min`)
      fx2.foreach { x =>
        println(s"GOT IT: ${x}")
      }*/

      val api = new BittrexApi()
      import Bittrex._
      val fx =  api.getMarkets
      println(s"fx: ${fx}")
      fx.foreach { x =>
        println(s"GOT IT: ${x}")
      }

     /* println("calling STOCHF")
      val fx3 = AlphaVantage.AlphaVantageStochasticFast.get("MSFT", AlphaVantage.Interval.`1min`, 10, 3)
      fx3.foreach { x =>
        println(s"GOT IT: ${x}")
      }*/
      Thread.currentThread.join()


    }catch{
      case t:Throwable =>
        t.printStackTrace()
    }

  }

}
