package service

import org.scalatest.funsuite.AsyncFunSuite

class TextToSpeechTest extends AsyncFunSuite {

  test("1") {
    TextToSpeech
      .toMp3Bytes("Test")
      .map {
        case bytes if bytes.length == 11664 => succeed
        case bytes                          => fail(s"expected 11664 bytes, but was ${bytes.length}")
      }
  }
}
