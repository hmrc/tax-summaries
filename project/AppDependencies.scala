import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-27" % "5.3.0",
    "uk.gov.hmrc"                %% "domain"                    % "6.1.0-play-27",
    "uk.gov.hmrc"                %% "time"                      % "3.25.0",
    "com.github.fge"             % "json-schema-validator"      % "2.2.6",
    "org.reactivemongo"          %% "play2-reactivemongo"       % "0.18.8-play27",
    "com.typesafe.scala-logging" %% "scala-logging"             % "3.9.2"
  )

  val test: Seq[ModuleID] = Seq(
    "com.typesafe.play"      %% "play-test"                 % PlayVersion.current,
    "org.scalatestplus.play" %% "scalatestplus-play"        % "4.0.0",
    "org.jsoup"              % "jsoup"                      % "1.13.1",
    "com.github.fge"         % "json-schema-validator"      % "2.2.6",
    "org.mockito"            % "mockito-all"                % "1.10.19",
    "org.scalacheck"         %% "scalacheck"                % "1.14.3",
    "com.github.tomakehurst" % "wiremock-jre8"              % "2.27.2",
    "org.pegdown"            %  "pegdown"                   % "1.6.0"
  ).map(_ % "test,it")

  val all: Seq[ModuleID] = compile ++ test
}
