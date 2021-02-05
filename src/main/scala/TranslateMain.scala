import googleTranslate.GoogleTranslate

import scala.concurrent.Await
import scala.concurrent.duration._

object TranslateMain {
  def main(args: Array[String]): Unit = {
      val src = "de"
      val dest = "en"
      val translated = Await.result(GoogleTranslate(text = args, src = src, dest = dest), 1.minute)

      println(s"translated: ${translated}")
  }
}
