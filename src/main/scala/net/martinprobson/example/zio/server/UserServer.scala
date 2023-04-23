package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import net.martinprobson.example.zio.common.ZIOApplication
import net.martinprobson.example.zio.repository.{
  InMemoryUserRepository,
  UserRepository
}
//import zio.http.ServerConfig.LeakDetectionLevel

object UserServer extends ZIOApplication:

  private def program: RIO[UserRepository, Unit] = for
    _ <- ZIO.logInfo(s"Starting server ")
    server <- Server
      .serve(UserApp().withDefaultErrorResponse)
      .flatMap(port => ZIO.logInfo(s"Server start on port: $port"))
      .provideSome[UserRepository](
        Server.live,
        ZLayer.succeed(Server.Config.default.port(8085))
      )
      .fork
    _ <- Console.readLine("Press enter to stop the server\n")
    _ <- Console.printLine("Interrupting server")
    _ <- server.interrupt
  yield ()

  def run: Task[Unit] = program.provide(InMemoryUserRepository.layer)

end UserServer
