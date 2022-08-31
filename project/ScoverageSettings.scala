import sbt.Setting
import scoverage.ScoverageKeys

object ScoverageSettings {

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

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 91,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}
