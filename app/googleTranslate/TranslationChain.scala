package googleTranslate

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

object TranslationChain {
  val defaultLang = "en"
  val defaultChain = Seq("ar", "bn", "zh-tw", "cs", "nl", "eo", "fi", "el", "ht", "iw", "ta", "uz", "vi", "cy", "xh", "yo")

  def apply(texts: Seq[String], lang: String = defaultLang, chain: Seq[String] = defaultChain)(
    implicit as: ActorSystem,
    ex: ExecutionContext
  ): Future[Map[String, String]] = {
    val fullChain = lang +: chain :+ lang
    //    println(fullChain)

    var translation = Future.successful(texts.map(x => (x, x)).toMap)

    for (Seq(src, dest) <- fullChain.sliding(2)) {
      translation = translation.flatMap(step(_, src = src, dest = dest))
    }

    translation
  }

  def step(translations: Map[String, String], src: String, dest: String)(
    implicit as: ActorSystem,
    ex: ExecutionContext
  ): Future[Map[String, String]] = {
    GoogleTranslate(src = src, dest = dest, texts = translations.values.toSeq)
      .map { nextTranslation =>
//        println(s"translation step: ${src} => ${dest}\n${nextTranslation}")
        Thread.sleep(3000)
        translations.transform((_, value) => nextTranslation(value))
      }
  }
}
