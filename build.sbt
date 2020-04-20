import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.core.PlayVersion.current
import scoverage.ScoverageKeys

val appName = "compliance-documents-api"

majorVersion := 0
scalaVersion := "2.12.11"

lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .settings(PlayKeys.playDefaultPort := 7053)


libraryDependencies ++= Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % "1.7.0",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "com.typesafe.play" %% "play-test" % current % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % "test, it",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "com.github.tomakehurst" % "wiremock-standalone" % "2.26.3" % "test, it"

)

ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*Routes.*;.*GuiceInjector;"
ScoverageKeys.coverageMinimum := 100
ScoverageKeys.coverageFailOnMinimum := true
ScoverageKeys.coverageHighlighting := true

publishingSettings
resolvers += Resolver.jcenterRepo
integrationTestSettings
coverageEnabled in(Test, compile) := true

enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
