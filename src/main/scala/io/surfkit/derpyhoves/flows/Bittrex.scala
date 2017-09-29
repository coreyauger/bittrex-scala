package io.surfkit.derpyhoves.flows

import java.util.UUID

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import play.api.libs.json._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.util.ByteString
import org.joda.time.DateTimeZone

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import org.joda.time.DateTime


/*val bs: Future[ByteString] = response.entity.toStrict(1 minute).map { _.data }
val s: Future[String] = bs.map(_.utf8String) // if you indeed need a `String`
s.foreach(println)
*/

/**
  * Created by suroot on 18/07/17.
  */
object Bittrex{
  sealed trait BX

  val pattern = "yyyy-MM-dd'T'HH:mm:ss"
  implicit val dateFormat = Format[DateTime](Reads.jodaDateReads(pattern, str => str.split('.').head), Writes.jodaDateWrites(pattern))

  case class IntervalPrice(
                            ts: DateTime,
                            volume: Double,
                            open: Double,
                            close: Double,
                            high: Double,
                            low: Double
                          ) extends BX

  case class Market(
                     MarketCurrency: String,
                     BaseCurrency: String,
                     MarketCurrencyLong: String,
                     BaseCurrencyLong: String,
                     MinTradeSize: Double,
                     MarketName: String,
                     IsActive: Boolean,
                     Created: DateTime,
                     Notice: Option[String],
                     IsSponsored: Option[Boolean],
                     LogoUrl: Option[String]
                   ) extends BX
  implicit val marketWrites = Json.writes[Market]
  implicit val marketReads = Json.reads[Market]

  case class Tick(
                   Bid: Double,
                   Ask: Double,
                   Last: Double
                 ) extends BX
  implicit val tickWrites = Json.writes[Tick]
  implicit val tickReads = Json.reads[Tick]

  case class Currency(
                       Currency: String,
                       CurrencyLong: String,
                       MinConfirmation: Int,
                       TxFee: Double,
                       IsActive: Boolean,
                       CoinType: String,
                       BaseAddress: Option[String],
                       Notice: Option[String]
                     ) extends BX
  implicit val currencyWrites = Json.writes[Currency]
  implicit val currencyReads = Json.reads[Currency]

  case class MarketSummary(
                          MarketName: String,
                          High: Double,
                          Low: Double,
                          Volume: Double,
                          Last: Double,
                          BaseVolume: Double,
                          TimeStamp: DateTime,
                          Bid: Double,
                          Ask: Double,
                          OpenBuyOrders: Int,
                          OpenSellOrders: Int,
                          PrevDay: Double,
                          Created: DateTime,
                          DisplayMarketName: Option[String]
                          ) extends BX
  implicit val marketSummaryWrites = Json.writes[MarketSummary]
  implicit val marketSummaryReads = Json.reads[MarketSummary]

  case class Fills(
                    Quantity: Double,
                    Rate: Double
                  ) extends BX
  implicit val orderWrites = Json.writes[Fills]
  implicit val orderReads = Json.reads[Fills]

  case class OrderBook(buy: Seq[Fills], sell:Seq[Fills]) extends BX
  implicit val orderBookWrites = Json.writes[OrderBook]
  implicit val orderBookReads = Json.reads[OrderBook]

  case class MarketHistory(
                            Id: Long,
                            TimeStamp: DateTime,
                            Quantity: Double,
                            Price: Double,
                            Total: Double,
                            FillType: String,
                            OrderType: String) extends BX
  implicit val marketHistoryWrites = Json.writes[MarketHistory]
  implicit val marketHistoryReads = Json.reads[MarketHistory]

  trait OrderId extends BX{
    def uuid: String
  }
  case class BuyLimit(uuid: String) extends OrderId
  implicit val BuyLimitIdWrites = Json.writes[BuyLimit]
  implicit val BuyLimitIdReads = Json.reads[BuyLimit]

  case class SellLimit(uuid: String) extends OrderId
  implicit val SellLimitIdWrites = Json.writes[SellLimit]
  implicit val SellLimitIdReads = Json.reads[SellLimit]

  case class Empty(`null`: Option[String]) extends BX
  implicit val EmptyWrites = Json.writes[Empty]
  implicit val EmptyReads = Json.reads[Empty]

  case class Order(
                    AccountId: Option[String],
                    Uuid: Option[String],
                    OrderUuid: String,
                    Exchange: String,
                    OrderType: String,
                    Quantity: Double,
                    QuantityRemaining: Double,
                    Limit: Double,
                    CommissionPaid: Double,
                    Price: Double,
                    PricePerUnit: Option[Double],
                    Opened: DateTime,
                    Closed: Option[DateTime],
                    CancelInitiated: Boolean,
                    ImmediateOrCancel: Boolean,
                    IsConditional: Boolean,
                    Condition: Option[String],
                    ConditionTarget: Option[Double]
                      ) extends BX
  implicit val OpenOrderWrites = Json.writes[Order]
  implicit val OpenOrderReads = Json.reads[Order]

  case class AccountBalance(
                    Currency: String,
                    Balance: Double,
                    Available: Double,
                    Pending: Double,
                    CryptoAddress: Option[String],
                    Requested: Option[Boolean],
                    Uuid: Option[String]
                           ) extends BX
  implicit val AccountBalanceWrites = Json.writes[AccountBalance]
  implicit val AccountBalanceReads = Json.reads[AccountBalance]

  case class OrderHistory(
                    OrderUuid: String,
                    Exchange: String,
                    TimeStamp: DateTime,
                    OrderType: String,
                    Limit: Double,
                    Quantity: Double,
                    QuantityRemaining: Double,
                    Commission: Double,
                    Price: Double,
                    PricePerUnit: Option[Double],
                    IsConditional: Boolean,
                    Condition: Option[String],
                    ConditionTarget: Option[Double],
                    ImmediateOrCancel: Boolean
                               )  extends BX
  implicit val OrderHistoryWrites = Json.writes[OrderHistory]
  implicit val OrderHistoryReads = Json.reads[OrderHistory]

  trait Response extends BX{
    def success: Boolean
    def message: String
  }
  case class ResponseSingle[T <: BX](success: Boolean, message: String, result: Option[T] ) extends Response
  case class ResponseList[T <: BX](success: Boolean, message: String, result: Option[Seq[T]] ) extends Response
  implicit val responseMarketWrites = Json.writes[ResponseList[Market]]
  implicit val responseMarketReads = Json.reads[ResponseList[Market]]
  implicit val responseTickWrites = Json.writes[ResponseSingle[Tick]]
  implicit val responseTickReads = Json.reads[ResponseSingle[Tick]]
  implicit val responseCurrencyWrites = Json.writes[ResponseList[Currency]]
  implicit val responseCurrencyReads = Json.reads[ResponseList[Currency]]
  implicit val responseMarketSummaryWrites = Json.writes[ResponseList[MarketSummary]]
  implicit val responseMarketSummaryReads = Json.reads[ResponseList[MarketSummary]]
  implicit val responseOrderBookWrites = Json.writes[ResponseList[OrderBook]]
  implicit val responseOrderBookReads = Json.reads[ResponseList[OrderBook]]
  implicit val responseMarketHistoryWrites = Json.writes[ResponseList[MarketHistory]]
  implicit val responseMarketHistoryReads = Json.reads[ResponseList[MarketHistory]]
  implicit val responseBuyLimitWrites = Json.writes[ResponseSingle[BuyLimit]]
  implicit val responseBuyLimitReads = Json.reads[ResponseSingle[BuyLimit]]
  implicit val responseSellLimitWrites = Json.writes[ResponseSingle[SellLimit]]
  implicit val responseSellLimitReads = Json.reads[ResponseSingle[SellLimit]]
  implicit val responseEmptyWrites = Json.writes[ResponseList[Empty]]
  implicit val responseEmptyReads = Json.reads[ResponseList[Empty]]
  implicit val responseOpenOrderWrites = Json.writes[ResponseList[Order]]
  implicit val responseOpenOrderReads = Json.reads[ResponseList[Order]]
  implicit val responseAccountBalanceWrites = Json.writes[ResponseList[AccountBalance]]
  implicit val responseAccountBalanceReads = Json.reads[ResponseList[AccountBalance]]
  implicit val responseAccountBalanceWrites2 = Json.writes[ResponseSingle[AccountBalance]]
  implicit val responseAccountBalanceReads2 = Json.reads[ResponseSingle[AccountBalance]]
  implicit val responseOrderHistoryWrites = Json.writes[ResponseList[OrderHistory]]
  implicit val responseOrderHistoryReads = Json.reads[ResponseList[OrderHistory]]

}

class BittrexInterval[T <: Bittrex.BX](function: String, interval: FiniteDuration, secret: String)(implicit system: ActorSystem, materializer: Materializer, um: Reads[T]) extends BittrexPoller(
  url = s"https://bittrex.com/api/v1.1${function}",
  interval = interval, apisecret = secret) with PlayJsonSupport{

  def json(): Source[Future[T], Cancellable] = super.apply().map{
    case scala.util.Success(response) => Unmarshal(response.entity).to[T]
    case scala.util.Failure(ex) =>
      ex.printStackTrace()
      Future.failed(ex)
  }
}

case class BittrexTicker(market: String, interval: FiniteDuration)(implicit system: ActorSystem, materializer: Materializer)
  extends BittrexInterval[Bittrex.ResponseSingle[Bittrex.Tick]](s"/public/getticker?market=${market}", interval, "publis")

case class BittrexMarketHistory(market: String, interval: FiniteDuration)(implicit system: ActorSystem, materializer: Materializer)
  extends BittrexInterval[Bittrex.ResponseList[Bittrex.MarketHistory]](s"/public/getmarkethistory?market=${market}", interval, "public"){

  var last = 0.0

  def intervalPrice: Source[Future[Bittrex.IntervalPrice], Cancellable] =
    super.json().map{ future =>
      future.map{ history =>
        val now = DateTime.now()
        val lastMin = now.plusMinutes(-1)
        val minuteHistory = history.result.getOrElse(Seq.empty).filter(x => x.TimeStamp.isAfter(lastMin))
        minuteHistory match{
          case Seq() =>
            Bittrex.IntervalPrice(now, 0, last, last, last, last)
          case _ =>
            val high = minuteHistory.map(_.Price).max
            val low = minuteHistory.map(_.Price).min
            val volume = minuteHistory.filter(_.OrderType == "SELL").map(_.Price).sum
            last = minuteHistory.head.Price
            Bittrex.IntervalPrice(now, volume, minuteHistory.last.Price, minuteHistory.head.Price, high, low)
        }

      }
    }
}


class BittrexApi(apiKey: String, secret: String)(implicit system: ActorSystem, materializer: Materializer) extends PlayJsonSupport {

  val baseAddress = "https://bittrex.com/api/v1.1"

  def nounce = new DateTime().getMillis() / 1000
  object api extends BittrexSignedRequester(secret)

  def responseUnmarshal[T <: Bittrex.BX](response: HttpResponse)(implicit um: Reads[T]):Future[T] = Unmarshal(response.entity).to[T]

  def getMarkets()(implicit um: Reads[Bittrex.ResponseList[Bittrex.Market]]) =
    api.get(s"${baseAddress}/public/getmarkets?apikey=${apiKey}&nonce=${nounce}").flatMap(x => responseUnmarshal(x) )

  def getCurrency()(implicit um: Reads[Bittrex.ResponseList[Bittrex.Currency]]) =
    api.get(s"${baseAddress}/public/getcurrencies?apikey=${apiKey}&nonce=${nounce}").flatMap(x => responseUnmarshal(x) )

  def getTicker(market: String)(implicit um: Reads[Bittrex.ResponseSingle[Bittrex.Tick]]) =
    api.get(s"${baseAddress}/public/getcurrencies?apikey=${apiKey}&nonce=${nounce}&market=${market}").flatMap(x => responseUnmarshal(x) )

  def getMarketSummaries ()(implicit um: Reads[Bittrex.ResponseList[Bittrex.MarketSummary]]) =
    api.get(s"${baseAddress}/public/getmarketsummaries?apikey=${apiKey}&nonce=${nounce}").flatMap(x => responseUnmarshal(x) )

  def getMarketSummary (market: String)(implicit um: Reads[Bittrex.ResponseList[Bittrex.MarketSummary]]) =
    api.get(s"${baseAddress}/public/getmarketsummary?apikey=${apiKey}&nonce=${nounce}&market=${market}").flatMap(x => responseUnmarshal(x) )

  def getOrderBook (market: String)(implicit um: Reads[Bittrex.ResponseList[Bittrex.OrderBook]]) =
    api.get(s"${baseAddress}/public/getorderbook?apikey=${apiKey}&nonce=${nounce}&market=${market}").flatMap(x => responseUnmarshal(x) )

  def getMarketHistory (market: String)(implicit um: Reads[Bittrex.ResponseList[Bittrex.MarketHistory]]) =
    api.get(s"${baseAddress}/public/getmarkethistory?apikey=${apiKey}&nonce=${nounce}&market=${market}").flatMap(x => responseUnmarshal(x) )

  def buyLimit (market: String, quantity: Double, rate: Double)(implicit um: Reads[Bittrex.ResponseSingle[Bittrex.BuyLimit]]) =
    api.get(s"${baseAddress}/market/buylimit?apikey=${apiKey}&nonce=${nounce}&market=${market}&quantity=${quantity}&rate=${rate}").flatMap(x => responseUnmarshal(x) )

  def sellLimit (market: String, quantity: Double, rate: Double)(implicit um: Reads[Bittrex.ResponseSingle[Bittrex.SellLimit]]) =
    api.get(s"${baseAddress}/market/selllimit?apikey=${apiKey}&nonce=${nounce}&market=${market}&quantity=${quantity}&rate=${rate}").flatMap(x => responseUnmarshal(x) )

  def cancel (uuid: String)(implicit um: Reads[Bittrex.ResponseSingle[Bittrex.Empty]]) =
    api.get(s"${baseAddress}/market/cancel?apikey=${apiKey}&nonce=${nounce}&uuid=${uuid}").flatMap(x => responseUnmarshal(x) )

  def getOpenOrders (market: String)(implicit um: Reads[Bittrex.ResponseList[Bittrex.Order]]) =
    api.get(s"${baseAddress}/market/getopenorders?apikey=${apiKey}&nonce=${nounce}&market=${market}").flatMap(x => responseUnmarshal(x) )

  def getBalances()(implicit um: Reads[Bittrex.ResponseList[Bittrex.AccountBalance]]) =
    api.get(s"${baseAddress}/account/getbalances?apikey=${apiKey}&nonce=${nounce}").flatMap(x => responseUnmarshal(x) )

  def getBalance(currency: String)(implicit um: Reads[Bittrex.ResponseSingle[Bittrex.AccountBalance]]) =
    api.get(s"${baseAddress}/account/getbalance?apikey=${apiKey}&nonce=${nounce}&currency=${currency}").flatMap(x => responseUnmarshal(x) )

  def getOrder(uuid: String)(implicit um: Reads[Bittrex.ResponseList[Bittrex.Order]]) =
    api.get(s"${baseAddress}/account/getorder?apikey=${apiKey}&nonce=${nounce}&uuid=${uuid}").flatMap(x => responseUnmarshal(x) )

  def getOrderHistory(market: Option[String] = None)(implicit um: Reads[Bittrex.ResponseList[Bittrex.OrderHistory]]) =
    api.get(s"${baseAddress}/account/getorderhistory?apikey=${apiKey}&nonce=${nounce}${market.map(m => s"&market=${m}").getOrElse("")}").flatMap(x => responseUnmarshal(x) )
}