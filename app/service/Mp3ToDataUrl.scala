package service

object Mp3ToDataUrl {
  def apply(mp3: Seq[Byte]): String =
    s"data:audio/mpeg;base64,${java.util.Base64.getEncoder.encodeToString(mp3.toArray)}"
}
