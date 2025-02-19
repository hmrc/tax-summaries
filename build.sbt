/*
 * Copyright 2024 HM Revenue & Customs
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

import sbt.*
import uk.gov.hmrc.DefaultBuildSettings

val appName = "tax-summaries"

ThisBuild / majorVersion := 3
ThisBuild / scalaVersion := "3.3.4"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    PlayKeys.playDefaultPort := 9323,
    ScoverageSettings.settings,
    libraryDependencies ++= AppDependencies.all
  )
  .settings(
    scalacOptions ++= Seq(
      "-unchecked",
      "-feature",
      "-Wvalue-discard",
      "-Werror",
      "-Wconf:msg=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:msg=unused&src=.*Routes\\.scala:s",
      "-Wvalue-discard",
      "-Xfatal-warnings",
      "-Wconf:msg=Flag.*repeatedly:s"
    )
  )

Test / Keys.fork := true
Test / parallelExecution := true

lazy val it = project
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings(),
    javaOptions += "-Dlogger.resource=logback-test.xml"
  )