name := "zio with zio-http, zio-streams and doobie example"
ThisBuild / scalaVersion := "3.3.4"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / organization := "net.martinprobson.example"

val zioVersion            = "2.1.14"
val zioConnectFileVersion = "0.4.4"
val zioLoggingVersion     = "2.4.0"
val zioHttpVersion        = "3.0.1"
val zioJsonVersion        = "0.7.3"
val zioConfigVersion      = "4.0.2"
val logbackVersion        = "1.4.6"

val commonDependencies = Seq(
  "dev.zio" %% "zio" % zioVersion,
  // zio json
  "dev.zio" %% "zio-json" % zioJsonVersion,
  // zio logging
  "dev.zio" %% "zio-logging" % zioLoggingVersion,
  "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
  // zio Config
  "dev.zio" %% "zio-config" % zioConfigVersion,
  "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.5.12",
  "ch.qos.logback" % "logback-core" % "1.5.12",
  // Testing
  "dev.zio" %% "zio-test" % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % zioVersion  % Test
)

lazy val root = project
  .in(file("."))
  .aggregate(common, client, server, files)
  .disablePlugins(AssemblyPlugin)
  .settings(Test / fork := true, run / fork := true)
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(commonSettings)

lazy val common = project
  .in(file("common"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= commonDependencies)
  .disablePlugins(AssemblyPlugin)
  .settings(Test / fork := true, run / fork := true)
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val files = project
        .in(file("files"))
        .dependsOn(common)
        .settings(commonSettings)
        .settings(assembly / mainClass := Some("net.martinprobson.example.zio,files.FileWriter"))
        .settings(libraryDependencies ++=
                commonDependencies ++
                Seq("dev.zio" %% "zio-streams" % zioVersion,
                    "dev.zio" %% "zio-connect-file" % zioConnectFileVersion)
        )
        .settings(Test / fork := true, run / fork := true)
        .settings(assemblySettings)

lazy val client = project
  .in(file("client"))
  .dependsOn(common,files)
  .settings(commonSettings)
  .settings(libraryDependencies ++=
    commonDependencies ++
    Seq( "dev.zio" %% "zio-http" % zioHttpVersion)
    )
  .settings(Test / fork := true, run / fork := true)
  .settings(assembly / mainClass := Some("net.martinprobson.example.zio.client.Main"))
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .settings(assemblySettings)

lazy val server = project
  .in(file("server"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(assembly / mainClass := Some("net.martinprobson.example.zio.server.UserServer"))
  .settings(libraryDependencies ++=
    commonDependencies ++
    Seq("dev.zio" %% "zio-http" % zioHttpVersion,
        // Doobie
        "org.tpolecat" %% "doobie-core" % "1.0.0-RC6",
        "org.tpolecat" %% "doobie-hikari" %  "1.0.0-RC6",
        // CATS / Zio interop
        "dev.zio" %% "zio-interop-cats" % "23.1.0.3",
        // Logging (Cats)
        "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
        // Logging
        "ch.qos.logback" % "logback-classic" % "1.5.12",
        "ch.qos.logback" % "logback-core" % "1.5.12",
        // Db Drivers
        "mysql" % "mysql-connector-java" % "8.0.33",
        "com.h2database" % "h2" % "1.4.200")
    )
  .settings(Test / fork := true, run / fork := true)
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(assemblySettings)
  .settings(dockerExposedPorts := Seq(8085,8085))

lazy val compilerOptions = Seq(
  "-deprecation",         // Emit warning and location for usages of deprecated APIs.
  "-explaintypes",        // Explain type errors in more detail.
  "-Xfatal-warnings",     // Fail the compilation if there are any warnings.
  "-encoding",
  "utf8",
  "-Yretain-trees"        // This is needed for zio-json (see https://github.com/zio/zio-json/issues/779)
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions
)

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := name.value + ".jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case "reference.conf"              => MergeStrategy.concat
    case "module-info.class"           => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
