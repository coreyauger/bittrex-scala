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
object Bitrex{
  sealed trait BX

  val API_KEY = "XXX"

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
                   ) extends BX
  implicit val marketWrites = Json.writes[Market]
  implicit val marketReads = Json.reads[Market]


  case class Response[T <: BX](success: Boolean, message: String, result: Seq[T]) extends BX
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

class BittrexInterval[T <: Bitrex.BX(function: String,symbol: String, interval: FiniteDuration, tz: DateTimeZone)(implicit system: ActorSystem, materializer: Materializer, um: Reads[T]) extends BittrexPoller(
  url = s"https://www.alphavantage.co/query?function=${function}&symbol=${symbol}&interval=${interval}&apikey=${Bitrex.API_KEY}",
  interval = interval, Some(tz)) with PlayJsonSupport{

  def json(): Source[Try[Future[T]], Cancellable] = super.apply().map{
    case scala.util.Success(response) => scala.util.Success(Unmarshal(response.entity).to[T])
    case scala.util.Failure(ex) => scala.util.Failure(ex)
  }
}

//case class BittrexTimeSeries(symbol: String, interval: AlphaVantage.Interval, tz: DateTimeZone)(implicit system: ActorSystem, materializer: Materializer)
  //extends BittrexInterval[AlphaVantage.TimeSeriesResponse]("TIME_SERIES_INTRADAY", symbol, interval, tz)



class BittrexApi(implicit system: ActorSystem, materializer: Materializer) extends PlayJsonSupport {

  val aoiKey = "390521dbfd4040c28e6f536e209cb94"

  def nounce = new DateTime().getMillis() / 1000
  object api extends BittrexSignedRequester

  def getMarkets()(implicit um: Reads[Bitrex.Response[Bitrex.Market]]) = {
    api.get(s"https://bittrex.com/api/v1.1/public/getmarkets?apikey=${aoiKey}&nonce=${nounce}").flatMap { response =>
      /*val bs: Future[ByteString] = response.entity.toStrict(1 minute).map { _.data }
      val s: Future[String] = bs.map(_.utf8String) // if you indeed need a `String`
      s.foreach(println)
*/
      Unmarshal(response.entity).to[Bitrex.Response[Bitrex.Market]]
    }
  }
}