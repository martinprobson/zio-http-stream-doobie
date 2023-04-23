package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import net.martinprobson.example.zio.common.ZIOApplication
import net.martinprobson.example.zio.repository.{InMemoryUserRepository, UserRepository}
import zio.http.ServerConfig.LeakDetectionLevel

object UserServer extends ZIOApplication:

  private val port = 8085

  private val config = ServerConfig
    .default
    .port(port)
    .maxThreads(100)
    .keepAlive(false)
    .leakDetection(LeakDetectionLevel.PARANOID)

  private def program: RIO[UserRepository, Unit] = for
    _ <- ZIO.logInfo(s"Starting server on port: $port")
    server <- Server
      .serve(UserApp())
      .provideSome[UserRepository](
        ServerConfig.live(config),
        Server.live
      )
      .fork
    _ <- Console.readLine("Press enter to stop the server\n")
    _ <- Console.printLine("Interrupting server")
    _ <- server.interrupt
  yield ()

  def run: Task[Unit] = program.provide(InMemoryUserRepository.layer)

end UserServer
