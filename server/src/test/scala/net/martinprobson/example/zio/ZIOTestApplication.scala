package net.martinprobson.example.zio

import zio.config.typesafe.TypesafeConfigProvider
import zio.logging.*
import zio.logging.LogFormat.*
import zio.logging.backend.SLF4J
import zio.test.*
import zio.{Runtime, ZIOAppArgs, ZLayer}

trait ZIOTestApplication extends ZIOSpecDefault {

  /** Define our own log format to be passed to slf4j logger, that includes the
    * fiber id.
    */
  private val logFormat: LogFormat =
    LogFormat.allAnnotations +
      bracketed(LogFormat.fiberId) +
      text(" - ") +
      LogFormat.line +
      LogFormat.cause

  /** Remove the default logger and replace with our slf4j custom log format.
    */
  override val bootstrap: ZLayer[Any, Any, TestEnvironment] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(logFormat) >>>
      Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath) >>>
      testEnvironment
}
