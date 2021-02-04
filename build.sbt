name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "2.13.3"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
//scalafmtOnCompile in ThisBuild := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.2.3",
  "com.typesafe.akka" %% "akka-stream" % "2.6.12"
)

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test

{
  val version = "0.13.0"
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core"           % version,
    "io.circe" %% "circe-generic"        % version,
    "io.circe" %% "circe-generic-extras" % version,
    "io.circe" %% "circe-parser"         % version
  )
}
