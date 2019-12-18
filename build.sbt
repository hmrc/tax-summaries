import play.sbt.routes.RoutesKeys._
import sbt._
import sbt.Keys._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys.routesGenerator
import uk.gov.hmrc.versioning.SbtGitVersioning
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtOnCompile
import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc._
import DefaultBuildSettings._

val appName = "tax-summaries"

lazy val plugins: Seq[Plugins] =
  Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)

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
    ScoverageKeys.coverageMinimum := 80,
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
    majorVersion := 1,
    libraryDependencies ++= AppDependencies.all,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := StaticRoutesGenerator,
    scalafmtOnCompile := true,
    resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)
  )
