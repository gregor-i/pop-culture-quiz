name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "2.13.3"
scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
//scalafmtOnCompile in ThisBuild := true

libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.1.0"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
