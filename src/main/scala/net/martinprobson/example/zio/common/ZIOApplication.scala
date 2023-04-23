package net.martinprobson.example.zio.common

import zio.{ZIOAppDefault, ZLayer, ZIOAppArgs, Runtime}
import zio.logging.*
import zio.logging.LogFormat.*
import zio.logging.backend.SLF4J

trait ZIOApplication extends ZIOAppDefault {

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
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    // Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(logFormat)
}
