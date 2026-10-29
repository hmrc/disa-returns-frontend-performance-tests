import sbt._

object Dependencies {

  val test = Seq(
    "com.typesafe" % "config"                  % "1.4.9" % Test,
    "uk.gov.hmrc"          %% "performance-test-runner"   % "6.3.0"         % Test,
    "com.typesafe.play"    %% "play-json"                 % "2.10.8"        % Test,
    "org.playframework"    %% "play-ahc-ws-standalone"    % "3.0.13"        % Test,
    "org.apache.pekko"     %% "pekko-stream"              % "1.7.0"         % Test,
    "org.scalatest"        %% "scalatest"                 % "3.2.20"        % Test
  )

}
