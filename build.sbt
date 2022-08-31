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

lazy val IntegrationTest = config("it") extend Test

lazy val plugins: Seq[Plugins] = Seq(
    play.sbt.PlayScala,
    SbtAutoBuildPlugin,
    SbtGitVersioning,
    SbtDistributablesPlugin
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
      PlayKeys.playDefaultPort := 9323,
      publishingSettings,
      ScoverageSettings.settings,
      scalaSettings,
      defaultSettings(),
      majorVersion := 1,
      scalaVersion := "2.13.8",
      libraryDependencies ++= AppDependencies.all ++ JacksonOverrides.allJacksonOverrides,
      retrieveManaged := true,
      update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := InjectedRoutesGenerator,
      scalafmtOnCompile := true,
      resolvers += Resolver.jcenterRepo
  )
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(scalacOptions ++= Seq(
      "-Werror",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
  ))
