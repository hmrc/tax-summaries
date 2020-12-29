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

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.0.15"
val silencerVersion = "1.6.0"

dependencyOverrides += "com.typesafe.akka" %% "akka-stream"    % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-actor"     % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion

val appName = "tax-summaries"

lazy val IntegrationTest = config("it") extend (Test)

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
    scalaVersion := "2.12.12",
    majorVersion := 1,
    libraryDependencies ++= AppDependencies.all,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator,
    scalafmtOnCompile := true,
    resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo)
  )
  .configs(IntegrationTest)
  .settings(DefaultBuildSettings.integrationTestSettings())
