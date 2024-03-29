name := "pop-culture-puzzle"

ThisBuild / scalaVersion := "3.1.2"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / scalafmtOnCompile := true
run / fork                    := true
test / fork                   := true
Test / parallelExecution      := false

enablePlugins(JavaAppPackaging)

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.19" cross CrossVersion.for3Use2_13
libraryDependencies += "com.typesafe.akka" %% "akka-stream"      % "2.6.19" cross CrossVersion.for3Use2_13
libraryDependencies += "com.typesafe.akka" %% "akka-http"        % "10.2.9" cross CrossVersion.for3Use2_13

// todo: remove, as soon as play is no longer needed
libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.19" cross CrossVersion.for3Use2_13

libraryDependencies += "org.fomkin"              %% "korolev-akka"         % "1.3.0"
libraryDependencies += "com.typesafe.play"       %% "play-jdbc"            % "2.8.15" cross CrossVersion.for3Use2_13
libraryDependencies += "com.typesafe.play"       %% "play-jdbc-evolutions" % "2.8.15" cross CrossVersion.for3Use2_13
libraryDependencies += "com.zaxxer"               % "HikariCP"             % "5.0.1"
libraryDependencies += "org.postgresql"           % "postgresql"           % "42.3.6"
libraryDependencies += "org.playframework.anorm" %% "anorm"                % "2.6.10" cross CrossVersion.for3Use2_13
libraryDependencies += "org.playframework.anorm" %% "anorm-postgres"       % "2.6.10" cross CrossVersion.for3Use2_13
libraryDependencies += "net.ruippeixotog"        %% "scala-scraper"        % "2.2.1" cross CrossVersion.for3Use2_13
libraryDependencies += "org.scalatest" %% "scalatest"       % "3.2.12" % Test exclude ("org.scala-lang.modules", "scala-xml_3")
libraryDependencies += "io.lemonlabs"  %% "scala-uri"       % "4.0.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.3.0-alpha14"
libraryDependencies += "io.circe"      %% "circe-core"      % "0.14.2"
libraryDependencies += "io.circe"      %% "circe-parser"    % "0.14.2"
libraryDependencies += "io.circe"      %% "circe-generic"   % "0.14.2"

// Synthesizing speech
resolvers += Resolver.jcenterRepo
libraryDependencies += "de.dfki.mary" % "voice-cmu-slt-hsmm" % "5.2.1"
libraryDependencies += "de.sciss"     % "jump3r"             % "1.0.5"
