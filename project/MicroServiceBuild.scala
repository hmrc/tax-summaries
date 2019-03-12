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

  val compile = Seq(
    filters,
    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "org.json4s" %% "json4s-jackson" % json4sJacksonVersion,
    "uk.gov.hmrc" %% "time" % "3.3.0",
    "uk.gov.hmrc" %% "json-encryption" % jsonEncryptionVersion
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
        "org.mockito" % "mockito-all" % mockitoAllVersion
      )
    }.test
  }

  def apply() = compile ++ Test()

}