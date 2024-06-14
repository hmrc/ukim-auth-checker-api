import uk.gov.hmrc.DefaultBuildSettings
import play.sbt.PlayImport._

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("ukim-auth-checker-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    PlayKeys.playDefaultPort := 10160,
    resolvers += Resolver.jcenterRepo, // Keep the existing resolver
    resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
  )
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
