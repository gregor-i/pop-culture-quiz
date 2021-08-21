package dataprocessing.service

import marytts.LocalMaryInterface
import marytts.util.data.audio.MaryAudioUtils
import org.slf4j.LoggerFactory

import java.io.{File, FileInputStream}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object TextToSpeech {
  private val mary: Try[LocalMaryInterface] = Try(
    new LocalMaryInterface
  )

  private val logger = LoggerFactory.getLogger(this.getClass)

  def toMp3Bytes(text: String)(implicit ex: ExecutionContext): Future[Seq[Byte]] = {
    val processId = UUID.randomUUID().toString
    val wavFile   = File.createTempFile(processId, ".wav")
    val mp3File   = File.createTempFile(processId, ".mp3")

    val process = for {
      mary <- Future.fromTry(this.mary)
      _ <- Future {
        val audio   = mary.generateAudio(text)
        val samples = MaryAudioUtils.getSamplesAsDoubleArray(audio)
        MaryAudioUtils.writeWavFile(samples, wavFile.getAbsolutePath, audio.getFormat)
      }
      _ <- Future {
        new ConvertWavToMp3().run(wavFile, mp3File)
      }
      bytes <- Future {
        val bis = new FileInputStream(mp3File)
        val ret = LazyList
          .continually(bis.read())
          .takeWhile(_ != -1)
          .map(_.toByte)
          .toList
        logger.info(s"converted ${text.length} byte of text to mp3 file with size ${ret.size / 1024} kilobytes.")
        bis.close()
        ret
      }
      _ <- Future(wavFile.delete())
      _ <- Future(mp3File.delete())
    } yield bytes
    process.failed.foreach(logger.warn("text could not be transformed to audio.", _))
    process
  }
}
