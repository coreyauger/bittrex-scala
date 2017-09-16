package io.surfkit.derpyhoves.flows

import akka.NotUsed
import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import org.joda.time.DateTimeZone
import org.joda.time.DateTime
import com.roundeights.hasher.Implicits._

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

trait BittrexInternals{
  val apisecret = "XXX"
}

/**
  * Created by suroot on 18/07/17.
  */
class BittrexPoller(url: String, interval: FiniteDuration)(implicit system: ActorSystem, materializer: Materializer) extends BittrexInternals {
  import scala.concurrent.duration._
  val apisign = url.hmac(apisecret).sha512.hex
  println(
    s"""
       |curl --header "apisign:${apisign}" "${url}"
       """.stripMargin)
  val request: _root_.akka.http.scaladsl.model.HttpRequest = RequestBuilding.Get(Uri(url)).addHeader(RawHeader("apisign", apisign))
  val source: Source[HttpRequest, Cancellable] = Source.tick(0.seconds, interval, request)
  val sourceWithDest: Source[Try[HttpResponse], Cancellable] = source.map(req ⇒ (req, NotUsed)).via(Http().superPool[NotUsed]()).map(_._1)

  def apply(): Source[Try[HttpResponse], Cancellable] = sourceWithDest

  def shutdown = {
    Http().shutdownAllConnectionPools()
  }
}

class BittrexSignedRequester(implicit system: ActorSystem, materializer: Materializer) extends BittrexInternals{
  def get(url: String) = {
    val apisign = url.hmac(apisecret).sha512.hex
    println(
      s"""
         |curl --header "apisign:${apisign}" "${url}"
       """.stripMargin)
    Http().singleRequest(HttpRequest(uri = url).addHeader(RawHeader("apisign", apisign)))
  }
}
