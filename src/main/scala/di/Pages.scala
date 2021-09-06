package di

import frontend.{Page, pages}

import scala.concurrent.ExecutionContext

class Pages(agents: Agents, repo: Repo)(implicit ex: ExecutionContext) {
  object admin {
    val indexPage        = new pages.admin.IndexPage(repo.translationRepo)
    val agentsPage       = new pages.admin.AgentsPage(agents)
    val moviePage        = new pages.admin.MoviePage(repo.movieRepo)
    val moviesPage       = new pages.admin.MoviesPage(repo.movieRepo)
    val translationsPage = new pages.admin.TranslationsPage(repo.translationRepo)
  }

  object game {
    val questionPage = new pages.game.QuestionPage(repo.questionService)
    val indexPage    = new pages.game.IndexPage(questionPage)
    val roomPage     = new pages.game.RoomPage()
  }

  val all: Seq[Page[_]] = Seq(
    game.indexPage,
    game.questionPage,
    game.roomPage,
    admin.indexPage,
    admin.agentsPage,
    admin.moviePage,
    admin.moviesPage,
    admin.translationsPage
  )
}
