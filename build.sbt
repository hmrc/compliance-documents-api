import play.core.PlayVersion.current
import sbt.IntegrationTest
import sbt.Tests.{Group, SubProcess}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "compliance-documents-api"
val silencerVersion = "1.7.0"

majorVersion := 0
scalaVersion := "2.13.10"


lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 7053)

scalacOptions += "-Wconf:src=routes/.*:s"
scalacOptions +=  "-Wconf:cat=unused-imports&src=html/.*:s"
val bootstrapVersion = "7.9.0"
libraryDependencies ++= Seq(
  "uk.gov.hmrc"                 %% "bootstrap-backend-play-28"% bootstrapVersion,
  "com.github.java-json-tools"  % "json-schema-validator"     % "2.2.14"
)

lazy val testScope = "test, it"
libraryDependencies ++= Seq(
  "org.scalatest"            %% "scalatest"                % "3.2.9"          % testScope,
  "org.scalamock"            %% "scalamock"                % "5.2.0"           % testScope,
  "uk.gov.hmrc"              %% "bootstrap-test-play-28"   % bootstrapVersion  % Test,
  "com.vladsch.flexmark"     % "flexmark-all"              % "0.35.10"          % testScope,
  "org.pegdown"              % "pegdown"                   % "1.6.0"           % testScope,
  "org.scalatestplus.play"   %% "scalatestplus-play"       % "5.1.0"           % testScope,
  "com.github.tomakehurst"   % "wiremock-standalone"       % "3.0.0-beta-2"          % testScope
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
