package translation

import akka.actor.ActorSystem
import translation.google.GoogleTranslate

import scala.concurrent.{ExecutionContext, Future}

object TranslationChain {
  val defaultLang  = "en"
  val googleDefaultChain = Seq("ar", "bn", "zh-tw", "cs", "nl", "eo", "fi", "el", "ht", "iw", "ta", "uz", "vi", "cy", "xh", "yo")

  val systranDefaultChain = Seq(
    "ar", //Arabic,
    "en",
    "nl", //Dutch,
    "en", //English,
    "zh", //Chinese,
    "fr", //French,
    "en",
    "sv",  //Swedish
  )

  val defaultChain = systranDefaultChain

  def apply(texts: Seq[String], lang: String = defaultLang, chain: Seq[String] = defaultChain, service: TranslationService)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] = {
    val fullChain = lang +: chain :+ lang
    //    println(fullChain)

    var translation = Future.successful(texts.map(x => (x, x)).toMap)

    for (Seq(src, dest) <- fullChain.sliding(2)) {
      translation = translation.flatMap(step(_, src = src, dest = dest, service = service))
    }

    translation
  }

  def step(translations: Map[String, String], src: String, dest: String, service: TranslationService)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]] = {
    service(src = src, dest = dest, texts = translations.values.toSeq)
      .map { nextTranslation =>
        //        println(s"translation step: ${src} => ${dest}\n${nextTranslation}")
        Thread.sleep(3000)
        translations.transform((_, value) => nextTranslation(value))
      }
  }
}
