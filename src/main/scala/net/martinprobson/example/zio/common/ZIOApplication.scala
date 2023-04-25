package net.martinprobson.example.zio.common

import zio.config.typesafe.TypesafeConfigProvider
import zio.{ConfigProvider, Runtime, ZIOAppArgs, ZIOAppDefault, ZLayer}
import zio.logging.*
import zio.logging.LogFormat.*
import zio.logging.backend.SLF4J
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

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

  /** Configure the runtime: -
    *
    * <ul>
    * <li>Remove the default logger and replace with our slf4j custom log format.</li>
    * <li>Set the default config provider to be typesafe config.</li>
    * </ul>
    */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(logFormat) >>>
      Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath)
}
