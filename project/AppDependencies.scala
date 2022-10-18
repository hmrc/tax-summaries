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

import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playVersion                = "play-28"
  private val hmrcMongoVersion           = "0.71.0"
  private val jsonSchemaValidatorVersion = "2.2.6"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28" % "7.1.0",
    "uk.gov.hmrc"                %% "domain"                    % s"8.1.0-$playVersion",
    "com.github.fge"              % "json-schema-validator"     % jsonSchemaValidatorVersion,
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-$playVersion"  % hmrcMongoVersion,
    "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.5",
    "org.typelevel"              %% "cats-core"                 % "2.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"      %% "play-test"                     % PlayVersion.current,
    "org.scalatestplus.play" %% "scalatestplus-play"            % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-scalacheck"      % "3.1.0.0-RC2",
    "org.scalatestplus"      %% "scalatestplus-mockito"         % "1.0.0-M2",
    "org.jsoup"               % "jsoup"                         % "1.15.3",
    "com.github.fge"          % "json-schema-validator"         % jsonSchemaValidatorVersion,
    "org.mockito"             % "mockito-core"                  % "4.7.0",
    "org.scalacheck"         %% "scalacheck"                    % "1.16.0",
    "com.github.tomakehurst"  % "wiremock-jre8"                 % "2.33.2",
    "org.pegdown"             % "pegdown"                       % "1.6.0",
    "com.vladsch.flexmark"    % "flexmark-all"                  % "0.35.10",
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion
  ).map(_ % "test,it")

  val all: Seq[ModuleID]  = compile ++ test
}
