import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion

object AppDependencies {

  private val playVersion = "play-28"

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28" % "5.25.0",
    "uk.gov.hmrc"                %% "domain"                    % s"8.1.0-$playVersion",
    "uk.gov.hmrc"                %% "time"                      % "3.25.0",
    "com.github.fge"             % "json-schema-validator"      % "2.2.6",
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-$playVersion"  % "0.70.0",
    "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.5",
    "org.typelevel"              %% "cats-core"                 % "2.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.scalatestplus.play" %% "scalatestplus-play"        % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-scalacheck"  % "3.1.0.0-RC2",
    "org.scalatestplus"      %% "scalatestplus-mockito"     % "1.0.0-M2",
    "org.jsoup"              % "jsoup"                      % "1.15.2",
    "com.github.fge"         % "json-schema-validator"      % "2.2.6",
    "org.mockito"            % "mockito-all"                % "1.10.19",
    "org.scalacheck"         %% "scalacheck"                % "1.16.0",
    "com.github.tomakehurst" % "wiremock-jre8"              % "2.31.0", //Updating this causes errors due to a conflicting Jackson dependency with (I believe) Play-Bootstrap
    "org.pegdown"            %  "pegdown"                   % "1.6.0",
    "com.vladsch.flexmark"   % "flexmark-all"               % "0.35.10", // You can update this once Scala 2.13 is in place. See - https://github.com/hmrc/individuals-disclosures-api/blob/main/project/AppDependencies.scala
    "uk.gov.hmrc"            %% "tax-year"                  % "3.0.0",
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-test-$playVersion"  % "0.70.0"
  ).map(_ % "test,it")

  val all: Seq[ModuleID] = compile ++ test
}
