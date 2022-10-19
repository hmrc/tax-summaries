/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import play.sbt.routes.RoutesKeys._
import sbt._
import sbt.Keys._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys.routesGenerator
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
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  )
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
  .settings(
    DefaultBuildSettings.integrationTestSettings(),
    IntegrationTest / javaOptions += "-Dlogger.resource=logback-test.xml"
  )
  .settings(scalacOptions ++= Seq(
    /* "-Werror",*/
    "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
    "-Wconf:cat=unused&src=.*Routes\\.scala:s",
    "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
  ))
