package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import net.martinprobson.example.zio.common.ZIOApplication
import net.martinprobson.example.zio.common.config.AppConfig
import net.martinprobson.example.zio.repository.{
  DoobieUserRepository,
  InMemoryUserRepository,
  TransactorLive,
  UserRepository
}

object UserServer extends ZIOApplication:

  private def program: RIO[UserRepository, Unit] = for {
    config <- ZIO.config(AppConfig.config)
    _ <- ZIO.logInfo(s"Starting server on port ${config.port} ")
    server <- Server
      .serve(UserApp.routes.handleError(_ => Response.text("Error!")))
      .provideSome[UserRepository](
        Server.live,
        ZLayer.succeed(Server.Config.default.port(config.port))
      )
// Commented so this works in sbt    
//      .fork
//    _ <- Console.readLine("Press enter to stop the server\n")
//    _ <- Console.printLine("Interrupting server")
//    _ <- server.interrupt
  } yield ()

  // Run with an InMemory user repository.
  // def run: Task[Unit] = program.provide(InMemoryUserRepository.layer)

  // Run with a database backed UserRepository via Doobie.
  def run: Task[Unit] = program.provide(
    DoobieUserRepository.layer,
    TransactorLive.layer
  )
end UserServer
