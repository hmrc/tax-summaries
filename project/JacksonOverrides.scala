import sbt._

object JacksonOverrides {
  // To resolve a Jackson version conflict
  private val jacksonVersion = "2.13.3"
  private val jacksonDatabindVersion = "2.13.3"

  val jacksonOverrides: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.core" % "jackson-core",
    "com.fasterxml.jackson.core" % "jackson-annotations",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
  ).map(_ % jacksonVersion)

  val jacksonDatabindOverrides: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
  )

  val akkaSerializationJacksonOverrides: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names",
    "com.fasterxml.jackson.module" %% "jackson-module-scala",
  ).map(_ % jacksonVersion)

  val allJacksonOverrides: Seq[sbt.ModuleID] = jacksonDatabindOverrides ++ jacksonOverrides ++ akkaSerializationJacksonOverrides
}
