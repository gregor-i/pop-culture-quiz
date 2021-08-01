package model

import io.circe.Codec
import io.circe.generic.auto._

import java.time.ZonedDateTime

sealed trait QuoteCrawlerState

object QuoteCrawlerState {
  case object NotCrawled                                                      extends QuoteCrawlerState
  case class Crawled(title: String, numberOfQuotes: Int, time: ZonedDateTime) extends QuoteCrawlerState
  case object NotFound                                                        extends QuoteCrawlerState
  case class UnexpectedError(message: String)                                 extends QuoteCrawlerState

  implicit val codec: Codec[QuoteCrawlerState] = io.circe.generic.semiauto.deriveCodec[QuoteCrawlerState]
}
