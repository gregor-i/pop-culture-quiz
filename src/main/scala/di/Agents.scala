package di

import agent._
import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

class Agents(repo: Repo)(implicit as: ActorSystem, mat: Materializer, ex: ExecutionContext) {
  val imdbMoviePageAgent    = new IMDBMoviePageAgent(repo.movieRepo)
  val imdbQuotesAgent       = new IMDBQuotesAgent(repo.movieRepo)
  val speechAgent           = new SpeechAgent(repo.translationRepo)
  val googleTranslateAgent  = new GoogleTranslationAgent(repo.translationRepo)
  val systranTranslateAgent = new SystranTranslationAgent(repo.translationRepo)
  val all: Seq[Agent]       = Seq(imdbMoviePageAgent, imdbQuotesAgent, speechAgent, googleTranslateAgent, systranTranslateAgent)
}
