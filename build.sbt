import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "compliance-documents-api"

majorVersion := 0
scalaVersion := "3.3.4"

scalacOptions += "-Xlint:-missing-interpolator"
scalacOptions += "-Wconf:src=routes/.*:s"
scalacOptions +=  "-Wconf:cat=unused-imports&src=html/.*:s"

libraryDependencies ++= AppDependencies.all

lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedFiles := Seq("<empty>","Reverse.*",".*Routes.*",".*GuiceInjector","$anon", ".*javascript","testOnlyDoNotUseInAppConf.*").mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

resolvers += Resolver.jcenterRepo
integrationTestSettings()

javaOptions ++= Seq(
  "-Dpolyglot.js.nashorn-compat=true"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .configs(IntegrationTest)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 7053)
