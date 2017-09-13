package io.surfkit.derpyhoves.flows

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
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

/**
  * Created by suroot on 18/07/17.
  */
object AlphaVantage{
  sealed trait AV

  val API_KEY = "XXX"

  case class Interval(period: String) extends AV{
    def toDuration = this match{
      case AlphaVantage.Interval.`1min` => 1 minute
      case AlphaVantage.Interval.`5min` => 5 minutes
      case AlphaVantage.Interval.`15min` => 15 minutes
      case AlphaVantage.Interval.`30min` => 30 minutes
      case AlphaVantage.Interval.`60min` => 60 minutes
      case AlphaVantage.Interval.daily => 1 day
      case AlphaVantage.Interval.weekly => 7 days
      case AlphaVantage.Interval.monthly => 30 days
      case _ => 24 hours
    }
  }
  object Interval{
    val `1min` = Interval("1min")
    val `5min` = Interval("5min")
    val `15min` = Interval("15min")
    val `30min` = Interval("30min")
    val `60min` = Interval("60min")
    val daily = Interval("daily")
    val weekly = Interval("weekly")
    val monthly = Interval("monthly")
  }

  final case class IntervalPrice(
                `1. open`: String,
                `2. high`: String,
                `3. low`: String,
                `4. close`: String,
                `5. volume`: String) extends AV
  implicit val tickWrites = Json.writes[IntervalPrice]
  implicit val tickReads = Json.reads[IntervalPrice]

  final case class TimeSeries(series: Seq[(String, IntervalPrice)]) extends AV
  implicit val tsFormat: Format[TimeSeries] =
    new Format[TimeSeries] {
      override def reads(json: JsValue): JsResult[TimeSeries] = json match {
        case j: JsObject =>
          JsSuccess(TimeSeries(j.fields.map {
            case (name, size) =>
              size.validate[IntervalPrice] match {
                case JsSuccess(validSize, _) => (name, validSize)
                case e: JsError => return e
              }
          }))
        case _ =>
          JsError("Invalid JSON type")
      }

      override def writes(o: TimeSeries): JsValue = Json.toJson(o.series.toMap)
    }

  case class MetaData(
                 `1. Information`: String,
                 `2. Symbol`: String,
                 `3. Last Refreshed`: String,
                 `4. Interval`: String,
                 `5. Output Size`: String,
                 `6. Time Zone`: String
   ) extends AV
  implicit val mdWrites = Json.writes[MetaData]
  implicit val mdReads = Json.reads[MetaData]

  case class MetaDataDaily(
                       `1. Information`: String,
                       `2. Symbol`: String,
                       `3. Last Refreshed`: String,
                       `4. Output Size`: String,
                       `5. Time Zone`: String
                     ) extends AV
  implicit val mddWrites = Json.writes[MetaDataDaily]
  implicit val mddReads = Json.reads[MetaDataDaily]

  case class MetaDataEMA(
                          `1: Symbol`: String,
                          `2: Indicator`: String,
                          `3: Last Refreshed`: String,
                          `4: Interval`: String,
                          `5: Time Period`: Int,
                          `6: Series Type`: String,
                          `7: Time Zone`: String
                          ) extends AV
  implicit val mddEMAWrites = Json.writes[MetaDataEMA]
  implicit val mddEMAReads = Json.reads[MetaDataEMA]


  case class Market(
                     MarketCurrency: String,
                     BaseCurrency: String,
                     MarketCurrencyLong: String,
                     BaseCurrencyLong: String,
                     MinTradeSize: Double,
                     MarketName: String,
                     IsActive: Boolean,
                     Created: String,
                     Notice: Option[String],
                     IsSponsored: Option[Boolean],
                     LogoUrl: Option[String]
                   ) extends AV
  implicit val marketWrites = Json.writes[Market]
  implicit val marketReads = Json.reads[Market]


  case class Response[T <: AV](success: Boolean, message: String, result: Seq[T]) extends AV
  implicit val responseMarketWrites = Json.writes[Response[Market]]
  implicit val responseMarketReads = Json.reads[Response[Market]]


/*


  object AlphaVantageMACD extends PlayJsonSupport {
    import AlphaVantage._
    def get(symbol: String, interval: AlphaVantage.Interval)(implicit system: ActorSystem, materializer: Materializer, um: Reads[AlphaVantage.MACDResponse]): Future[AlphaVantage.MACDResponse] = {
      //println(s"https://www.alphavantage.co/query?function=MACD&series_type=close&symbol=${symbol}&interval=${interval.period}&apikey=${AlphaVantage.API_KEY}")
      Http().singleRequest(HttpRequest(uri = s"https://www.alphavantage.co/query?function=MACD&series_type=close&symbol=${symbol}&interval=${interval.period}&apikey=${AlphaVantage.API_KEY}")).flatMap { response =>
        Unmarshal(response.entity).to[AlphaVantage.MACDResponse]
      }
    }}

  object AlphaVantageStochasticFast extends PlayJsonSupport {
    import AlphaVantage._
    def get(symbol: String, interval: AlphaVantage.Interval, fastK: Int, fastD: Int, matype: AlphaVantage.MaType = AlphaVantage.MaType.SMA)(implicit system: ActorSystem, materializer: Materializer, um: Reads[AlphaVantage.MACDResponse]): Future[AlphaVantage.STOCHFResponse] = {
      val url = s"https://www.alphavantage.co/query?function=STOCHF&symbol=${symbol}&fastkperiod=${fastK}&fastdperiod=${fastD}&fastdmatype=${matype.code}&interval=${interval.period}&apikey=${AlphaVantage.API_KEY}"
      Http().singleRequest(HttpRequest(uri = url)).flatMap { response =>
        Unmarshal(response.entity).to[AlphaVantage.STOCHFResponse]
      }
    }}
*/


}

class BittrexInterval[T <: AlphaVantage.AV](function: String,symbol: String, interval: AlphaVantage.Interval, tz: DateTimeZone)(implicit system: ActorSystem, materializer: Materializer, um: Reads[T]) extends BittrexPoller(
  url = s"https://www.alphavantage.co/query?function=${function}&symbol=${symbol}&interval=${interval.period}&apikey=${AlphaVantage.API_KEY}",
  interval = interval.toDuration, Some(tz)) with PlayJsonSupport{

  def json(): Source[Try[Future[T]], Cancellable] = super.apply().map{
    case scala.util.Success(response) => scala.util.Success(Unmarshal(response.entity).to[T])
    case scala.util.Failure(ex) => scala.util.Failure(ex)
  }
}

//case class BittrexTimeSeries(symbol: String, interval: AlphaVantage.Interval, tz: DateTimeZone)(implicit system: ActorSystem, materializer: Materializer)
  //extends BittrexInterval[AlphaVantage.TimeSeriesResponse]("TIME_SERIES_INTRADAY", symbol, interval, tz)



class BittrexApi(implicit system: ActorSystem, materializer: Materializer) extends PlayJsonSupport {

  val aoiKey = "XXX"

  def nounce = new DateTime().getMillis() / 1000
  object api extends BittrexSignedRequester

  def getMarkets()(implicit um: Reads[AlphaVantage.Response[AlphaVantage.Market]]) = {
    api.get(s"https://bittrex.com/api/v1.1/public/getmarkets?apikey=${aoiKey}&nonce=${nounce}").flatMap { response =>
      /*val bs: Future[ByteString] = response.entity.toStrict(1 minute).map { _.data }
      val s: Future[String] = bs.map(_.utf8String) // if you indeed need a `String`
      s.foreach(println)
*/
      Unmarshal(response.entity).to[AlphaVantage.Response[AlphaVantage.Market]]
    }
  }
}