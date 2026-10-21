import sbt._

object Dependencies {

  val test = Seq(
    "com.typesafe" % "config"                  % "1.4.9" % Test,
    "uk.gov.hmrc"          %% "performance-test-runner"   % "6.3.0"         % Test
  )

}
