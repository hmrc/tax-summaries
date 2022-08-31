import play.sbt.routes.RoutesKeys._
import sbt._
import sbt.Keys._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys.routesGenerator
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtOnCompile
import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc._
import DefaultBuildSettings._

val appName = "tax-summaries"

val silencerVersion = "1.7.9"

lazy val IntegrationTest = config("it") extend Test

lazy val plugins: Seq[Plugins] =
  Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)

lazy val excludedPackages: Seq[String] = Seq(
  "<empty>",
  "app.*",
  "config.*",
  "Reverse.*",
  ".*AuthService.*",
  "models/.data/..*",
  "view.*",
  "uk.gov.hmrc.*",
  "prod.*",
  "testOnlyDoNotUseInAppConf.*"
)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 91,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
    PlayKeys.playDefaultPort := 9323,
    defaultSettings(),
    scoverageSettings,
    publishingSettings,
    scalaSettings,
    scalaVersion := "2.13.8",
    majorVersion := 1,
    libraryDependencies ++= AppDependencies.all ++ JacksonOverrides.allJacksonOverrides,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator,
    scalafmtOnCompile := true,
    resolvers += Resolver.jcenterRepo
  )
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
