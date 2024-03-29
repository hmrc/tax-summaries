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

import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playVersion                = "play-28"
  private val hmrcMongoVersion           = "1.6.0"
  private val jsonSchemaValidatorVersion = "2.2.14"
  private val bootstrapVersion           = "8.3.0"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc"       %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc"       %% "http-caching-client"             % s"9.6.0-$playVersion",
    "uk.gov.hmrc"       %% "domain"                          % s"8.2.0-$playVersion",
    "com.github.fge"     % "json-schema-validator"           % jsonSchemaValidatorVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                       % "2.9.0",
    "uk.gov.hmrc"       %% "tax-year"                   % "3.2.0",
    "uk.gov.hmrc"       %% "mongo-feature-toggles-client"    % "0.5.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "org.mockito"         %% "mockito-scala-scalatest"       % "1.17.14",
    "org.scalatestplus"   %% "scalacheck-1-17"               % "3.2.15.0",
    "org.jsoup"            % "jsoup"                         % "1.15.4",
    "com.github.fge"       % "json-schema-validator"         % jsonSchemaValidatorVersion,
    "com.vladsch.flexmark" % "flexmark-all"                  % "0.62.2",
    "uk.gov.hmrc.mongo"   %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion
  ).map(_ % "test,it")

  val all: Seq[ModuleID]  = compile ++ test
}