package io.surfkit.derpyhoves

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import io.surfkit.derpyhoves.flows._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json

import scala.concurrent.Await

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
          |{"success":true,"message":"","result":{"Currency":"BTC","Balance":0.08107020,"Available":0.08107020,"Pending":0.00000000,"CryptoAddress":null}}
        """.stripMargin

      val test = Json.parse(json).as[Bittrex.ResponseSingle[Bittrex.AccountBalance]]
      println(s"test: ${test}")


      val aoiKey = "XXX"
      val apiSecret = "XXX"

      val api = new BittrexApi(aoiKey, apiSecret)
      import Bittrex._

      val fx = api.getBalance("BTC")
      val f =  Await.ready(fx, 10 seconds)
      println(s"fx: ${f}")
/*
      val fx = api.getOrderHistory()
      val f =  Await.ready(fx, 10 seconds)
      println(s"fx: ${f}")

      val bx = api.getBalances()
      val b =  Await.ready(bx, 10 seconds)
      println(s"fx: ${b}")


      val ax = api.getOrder("uuid")
      ax.recover{
        case t:Throwable => t.printStackTrace()
      }
      val a =  Await.ready(ax, 10 seconds)
      println(s"fx: ${a}")
*/

     /* try {
        val ticker = BittrexTicker("BTC-LTC", 1 minute)
        ticker.json.runForeach{i =>
          print(".")
          i.map(x => x.map(println) ).recover{
            case t: Throwable =>
              println("error")
              t.printStackTrace()
              throw t
          }
        }(materializer)
      }catch{
        case t:Throwable =>
          t.printStackTrace()
      }
*/
/*
      try {
        val hist = BittrexMarketHistory("BTC-LTC", 1 minute)
        hist.intervalPrice.runForeach{i =>
          print(".")
          i.map(println).recover{
            case t: Throwable =>
              println("error")
              t.printStackTrace()
              throw t
          }
        }(materializer)
      }catch{
        case t:Throwable =>
          t.printStackTrace()
      }*/

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
