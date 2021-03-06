import play.core.PlayVersion.current
import sbt.IntegrationTest
import sbt.Tests.{Group, SubProcess}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "compliance-documents-api"
val silencerVersion = "1.7.0"

majorVersion := 0
scalaVersion := "2.12.11"


lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 7053)


libraryDependencies ++= Seq(
  "uk.gov.hmrc" %% "bootstrap-play-26" % "4.0.0",
  "com.github.java-json-tools"  % "json-schema-validator"     % "2.2.14",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
)

lazy val testScope = "test, it"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.0" % testScope,
  "org.scalamock" %% "scalamock" % "4.4.0" % testScope,
  "com.typesafe.play" %% "play-test" % current % testScope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % testScope,
  "org.pegdown" % "pegdown" % "1.6.0" % testScope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % testScope,
  "com.github.tomakehurst" % "wiremock-standalone" % "2.26.3" % testScope
)

ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*Routes.*;.*GuiceInjector;"
ScoverageKeys.coverageMinimum := 100
ScoverageKeys.coverageFailOnMinimum := true
ScoverageKeys.coverageHighlighting := true

publishingSettings
resolvers += Resolver.jcenterRepo
integrationTestSettings()
coverageEnabled in(Test, compile) := true

javaOptions ++= Seq(
  "-Dnashorn.regexp.impl=jdk"
)

testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map { test =>
  Group(test.name, Seq(test), SubProcess(
    ForkOptions().withRunJVMOptions(Vector("-Dtest.name=" + test.name, "-Dnashorn.regexp.impl=jdk"))
  ))
}
enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
scalacOptions ++= Seq(
  "-P:silencer:pathFilters=views;routes"
)
