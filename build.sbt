ThisBuild / organization := "net.martinprobson.example"
ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
  name := "zio-http-stream-quill",
  libraryDependencies ++= Seq(
    // zio
    "dev.zio" %% "zio" % "2.0.10",
    "dev.zio" %% "zio-streams" % "2.0.10",
    "dev.zio" %% "zio-connect-file" % "0.4.4",
    // zio logging
    "dev.zio" %% "zio-logging" % "2.1.10",
    "dev.zio" %% "zio-logging-slf4j" % "2.1.10",
    // zio http
    "dev.zio" %% "zio-http" % "3.0.0-RC1",
    "dev.zio" %% "zio-json" % "0.4.2",
    // quill
    "io.getquill" %% "quill-jdbc-zio" % "4.6.0.1",
    // zio Config
    "dev.zio" %% "zio-config" % "4.0.0-RC12",
    "dev.zio" %% "zio-config-magnolia" % "4.0.0-RC12",
    "dev.zio" %% "zio-config-typesafe" % "4.0.0-RC12",
    // Logging
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "ch.qos.logback" % "logback-core" % "1.2.11",
    // Db Drivers
    "mysql" % "mysql-connector-java" % "8.0.30",
    "com.h2database" % "h2" % "1.4.200",
    // Testing
    "dev.zio" %% "zio-test" % "2.0.10" % Test,
    "dev.zio" %% "zio-test-sbt" % "2.0.10" % Test
  )
)

scalacOptions ++= Seq(
  "-deprecation",         // Emit warning and location for usages of deprecated APIs.
  "-explaintypes",        // Explain type errors in more detail.
  "-explain",
  "-Xfatal-warnings",     // Fail the compilation if there are any warnings.
  "-encoding",
  "utf8",
  "-Yretain-trees"        // This is needed for zio-json (see https://github.com/zio/zio-json/issues/779)
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

