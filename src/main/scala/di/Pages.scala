package di

import frontend.pages

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
  }
}
