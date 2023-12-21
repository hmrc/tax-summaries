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

resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)

// To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.16.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables" % "2.2.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"         % "2.8.20")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"      % "2.0.7")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.5.0")
