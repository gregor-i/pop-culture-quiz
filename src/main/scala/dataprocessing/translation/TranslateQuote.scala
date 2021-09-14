package dataprocessing.translation

import akka.actor.ActorSystem
import model.{Blocking, Quote, Speech, TranslatedQuote}

import scala.concurrent.{ExecutionContext, Future}

object TranslateQuote {
  def apply(
      quote: Quote,
      lang: String = TranslationChain.defaultLang,
      chain: Seq[String],
      service: TranslationService
  )(implicit
      as: ActorSystem,
      ex: ExecutionContext
  ): Future[Quote] = {
    val texts = quote.statements.flatMap(_.items).map {
      case Blocking(blocking) => blocking
      case Speech(speech)     => speech
    }
    for {
      translation <- TranslationChain(texts = texts, lang = lang, chain = chain, service = service)
      translatedQuote = applyTranslation(quote, translation)
    } yield translatedQuote
  }

  def applyTranslation(quote: Quote, translation: Map[String, String]): Quote =
    quote.copy(
      statements = quote.statements
        .map { statement =>
          statement.copy(items = statement.items.map {
            case Blocking(blocking) => Blocking(translation(blocking))
            case Speech(speech)     => Speech(translation(speech))
          })
        }
    )
}
