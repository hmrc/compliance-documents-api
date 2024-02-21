import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val playSuffix = "play-30"
  val bootstrapVersion = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-backend-$playSuffix" % bootstrapVersion,
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0",
    "org.scalamock" %% "scalamock" % "5.2.0",
    "org.mockito"            %% "mockito-scala-scalatest"     % "1.17.30",
    "uk.gov.hmrc" %% s"bootstrap-test-$playSuffix" % bootstrapVersion,
      "org.scalatestplus.play"   %% "scalatestplus-play"            % "7.0.0"
  ).map(_ % "test, it")

  val all: Seq[ModuleID] = compile ++ test

}
