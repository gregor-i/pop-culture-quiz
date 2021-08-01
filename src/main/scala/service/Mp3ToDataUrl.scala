package service

import io.lemonlabs.uri.{DataUrl, MediaType}

object Mp3ToDataUrl {
  def apply(mp3: Seq[Byte]): String =
    DataUrl(
      mediaType = MediaType(rawValue = Some("audio/mpeg"), parameters = Vector.empty),
      base64 = true,
      data = mp3.toArray
    ).toStringPunycode
}
