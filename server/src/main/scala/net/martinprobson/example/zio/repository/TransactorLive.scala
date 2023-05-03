package net.martinprobson.example.zio.repository

import cats.effect.Resource
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import net.martinprobson.example.zio.common.config.AppConfig
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import zio.*
import zio.interop.catz.*

case class TransactorLive(config: AppConfig) {
  private def log: SelfAwareStructuredLogger[Task] = Slf4jLogger.getLogger[Task]

  private def transactor: ZIO[Scope, Throwable, Transactor[Task]] =
    (for
      _ <- Resource.eval[Task, Unit](log.info("Setting up transactor"))
      _ <- Resource.eval[Task, Unit](
        log.debug(s"DriverClassName = ${config.driverClassName}")
      )
      _ <- Resource.eval[Task, Unit](log.debug(s"url = ${config.url}"))
      _ <- Resource.eval[Task, Unit](log.debug(s"user = ${config.user}"))
      _ <- Resource.eval[Task, Unit](
        log.debug(s"password = ${config.password}")
      )
      ce <- ExecutionContexts.fixedThreadPool[Task](config.threads)
      xa <- HikariTransactor
        .newHikariTransactor[Task](
          config.driverClassName,
          config.url,
          config.user,
          config.password,
          ce
        )
    yield xa)
      .onFinalize(log.info("Finalize of transactor"))
      .toScopedZIO
}

//object TransactorLive {
//  val layer: ZLayer[AppConfig, Throwable, Transactor[Task]] =
//    ZLayer.scoped {
//      ZIO
//        .service[AppConfig]
//        .flatMap(cfg => TransactorLive(cfg).transactor)
//    }
//}
object TransactorLive {
  val layer: Layer[Throwable, Transactor[Task]] =
    ZLayer.scoped {
      for {
        config <- ZIO.config(AppConfig.config)
        t <- TransactorLive(config).transactor
      } yield t
    }
}
