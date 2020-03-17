import sbt._
import play.sbt.PlayImport._
import play.core.PlayVersion

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    filters,
    ws,
    "uk.gov.hmrc"       %% "microservice-bootstrap" % "10.4.0",
    "uk.gov.hmrc"       %% "domain"                 % "5.6.0-play-25",
    "org.json4s"        %% "json4s-jackson"         % "3.6.7",
    "uk.gov.hmrc"       %% "time"                   % "3.6.0",
    "uk.gov.hmrc"       %% "json-encryption"        % "4.4.0-play-25",
    "com.github.fge"     % "json-schema-validator"  % "2.2.6",
    "org.json4s"        %% "json4s-native"          % "3.5.5",
    "com.typesafe.play" %% "play-json"              % "2.6.0-M7", //This specific version resolves binary incompatibility
    "uk.gov.hmrc"       %% "auth-client"            % "2.31.0-play-25",
    "org.reactivemongo" %% "play2-reactivemongo"    % "0.18.8-play25"
  )

  val test: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"             %% "hmrctest"               % "3.9.0-play-25",
      "com.typesafe.play"       %% "play-test"              % PlayVersion.current,
      "org.scalatestplus.play"  %% "scalatestplus-play"     % "2.0.1",
      "org.jsoup"                % "jsoup"                  % "1.12.1",
      "org.json4s"              %% "json4s-jackson"         % "3.6.7",
      "com.github.fge"           % "json-schema-validator"  % "2.2.6",
      "org.json4s"              %% "json4s-native"          % "3.5.5",
      "org.mockito"              % "mockito-all"            % "1.10.19",
      "org.scalacheck"          %% "scalacheck"             % "1.14.3",
      "com.github.tomakehurst"   % "wiremock"               % "2.26.0"
    ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test
}
