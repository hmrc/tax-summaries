import sbt._

object MicroServiceBuild extends Build with MicroService {
  val appName = "tax-summaries"
  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "10.4.0"
  private val playUrlBindersVersion = "2.1.0"
  private val domainVersion = "5.3.0"
  private val json4sJacksonVersion = "3.2.10"
  private val jsonEncryptionVersion = "4.1.0"
  private val hmrcTestVersion = "3.6.0-play-25"
  private val scalaTestplusPlayVersion = "2.0.1"
  private val mockitoAllVersion = "1.10.19"
  private val jsonSchemaValidatorVersion = "2.2.6"
  private val json4sNativeVersion = "3.2.10"
  private val scalaCheckVersion = "1.13.4"

  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "org.json4s" %% "json4s-jackson" % json4sJacksonVersion,
    "uk.gov.hmrc" %% "time" % "3.3.0",
    "uk.gov.hmrc" %% "json-encryption" % jsonEncryptionVersion,
    "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
    "org.json4s" %% "json4s-native" % json4sNativeVersion,
    "com.typesafe.play" %% "play-json" % "2.6.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    val test : Seq[ModuleID] 
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestplusPlayVersion % scope,
        "org.jsoup" % "jsoup" % "1.7.3" % scope,
        "org.json4s" %% "json4s-jackson" % json4sJacksonVersion,
        "com.github.fge" % "json-schema-validator" % jsonSchemaValidatorVersion,
        "org.json4s" %% "json4s-native" % json4sNativeVersion,
        "org.mockito" % "mockito-all" % mockitoAllVersion,
        "org.scalacheck" %% "scalacheck" % scalaCheckVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test()

}