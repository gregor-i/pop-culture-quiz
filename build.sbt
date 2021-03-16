name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "2.13.5"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
//scalafmtOnCompile in ThisBuild := true

enablePlugins(PlayScala)
libraryDependencies += ws
libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "io.lemonlabs"            %% "scala-uri"          % "3.1.0"
libraryDependencies += "org.postgresql"          % "postgresql"          % "42.2.19"
libraryDependencies += "org.playframework.anorm" %% "anorm"              % "2.6.10"
libraryDependencies += "org.playframework.anorm" %% "anorm-postgres"     % "2.6.10"
libraryDependencies += "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "net.ruippeixotog"        %% "scala-scraper"      % "2.2.0"
libraryDependencies += "org.scalatest"           %% "scalatest"          % "3.2.6" % Test
libraryDependencies += "io.lemonlabs"            %% "scala-uri"          % "3.0.0"
libraryDependencies += "com.dripower"            %% "play-circe"         % "2812.0"

// Synthesizing speech
resolvers += Resolver.jcenterRepo
libraryDependencies += "de.dfki.mary" % "voice-cmu-slt-hsmm" % "5.2"
libraryDependencies += "de.sciss"     % "jump3r"             % "1.0.5"

{
  val version = "0.13.0"
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"           % version,
    "io.circe" %% "circe-generic"        % version,
    "io.circe" %% "circe-generic-extras" % version,
    "io.circe" %% "circe-parser"         % version
  )
}
