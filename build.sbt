name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation", "-Ymacro-annotations")
ThisBuild / scalafmtOnCompile := true

enablePlugins(SbtTwirl)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
  "com.typesafe.akka" %% "akka-stream"      % "2.6.8",
  "com.typesafe.akka" %% "akka-http"        % "10.2.5",
  "de.heikoseeberger" %% "akka-http-circe"  % "1.36.0"
)

libraryDependencies += "com.typesafe.play"       %% "play-jdbc"            % "2.8.8"
libraryDependencies += "com.typesafe.play"       %% "play-jdbc-evolutions" % "2.8.8"
libraryDependencies += "com.zaxxer"              % "HikariCP"              % "5.0.0"
libraryDependencies += "org.postgresql"          % "postgresql"            % "42.2.20"
libraryDependencies += "org.playframework.anorm" %% "anorm"                % "2.6.10"
libraryDependencies += "org.playframework.anorm" %% "anorm-postgres"       % "2.6.10"
libraryDependencies += "org.scalatestplus.play"  %% "scalatestplus-play"   % "5.1.0" % Test
libraryDependencies += "net.ruippeixotog"        %% "scala-scraper"        % "2.2.1"
libraryDependencies += "org.scalatest"           %% "scalatest"            % "3.2.9" % Test
libraryDependencies += "io.lemonlabs"            %% "scala-uri"            % "3.2.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.3.0-alpha9"

// Synthesizing speech
resolvers += Resolver.jcenterRepo
libraryDependencies += "de.dfki.mary" % "voice-cmu-slt-hsmm" % "5.2"
libraryDependencies += "de.sciss"     % "jump3r"             % "1.0.5"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core"           % "0.13.0",
  "io.circe" %% "circe-generic"        % "0.13.0",
  "io.circe" %% "circe-generic-extras" % "0.13.0",
  "io.circe" %% "circe-parser"         % "0.13.0"
)
