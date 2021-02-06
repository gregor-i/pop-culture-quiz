name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "2.13.3"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
//scalafmtOnCompile in ThisBuild := true

enablePlugins(PlayScala)
libraryDependencies += ws
libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "io.lemonlabs"            %% "scala-uri"          % "3.0.0"
libraryDependencies += "org.postgresql"          % "postgresql"          % "42.2.18"
libraryDependencies += "org.playframework.anorm" %% "anorm"              % "2.6.9"
libraryDependencies += "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "net.ruippeixotog"        %% "scala-scraper"      % "2.2.0"
libraryDependencies += "org.scalatest"           %% "scalatest"          % "3.2.2" % Test
libraryDependencies += "io.lemonlabs"            %% "scala-uri"          % "3.0.0"

{
  val version = "0.13.0"
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"           % version,
    "io.circe" %% "circe-generic"        % version,
    "io.circe" %% "circe-generic-extras" % version,
    "io.circe" %% "circe-parser"         % version
  )
}
