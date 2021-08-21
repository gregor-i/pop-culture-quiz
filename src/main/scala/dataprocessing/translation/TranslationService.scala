package dataprocessing.translation

import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}

trait TranslationService {
  def apply(texts: Seq[String], src: String, dest: String)(
      implicit as: ActorSystem,
      ex: ExecutionContext
  ): Future[Map[String, String]]

  def name: String

  def defaultChain: Seq[String]
}
