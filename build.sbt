import sbt.Tests.{Group, SubProcess}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "compliance-documents-api"
//val silencerVersion = "1.7.0"

majorVersion := 0
scalaVersion := "2.13.12"


lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 7053)

scalacOptions += "-Wconf:src=routes/.*:s"
scalacOptions +=  "-Wconf:cat=unused-imports&src=html/.*:s"

libraryDependencies ++= AppDependencies.all

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*Routes.*;.*GuiceInjector;",
    ScoverageKeys.coverageMinimumStmtTotal := 100,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

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