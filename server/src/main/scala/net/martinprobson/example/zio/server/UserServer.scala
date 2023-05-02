package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import net.martinprobson.example.zio.common.ZIOApplication
import net.martinprobson.example.zio.common.config.AppConfig
import net.martinprobson.example.zio.repository.{
  InMemoryUserRepository,
  QuillUserRepository,
  DataService,
  UserRepository
}
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase

object UserServer extends ZIOApplication:

  private def program: RIO[UserRepository, Unit] = for
    config <- ZIO.config(AppConfig.config)
    _ <- ZIO.logInfo(s"Starting server on port ${config.port} ")
    server <- Server
      .serve(UserApp().withDefaultErrorResponse)
      .provideSome[UserRepository](
        Server.live,
        ZLayer.succeed(Server.Config.default.port(config.port))
      )
      .fork
    _ <- Console.readLine("Press enter to stop the server\n")
    _ <- Console.printLine("Interrupting server")
    _ <- server.interrupt
  yield ()

//  def run: Task[Unit] = program.provide(InMemoryUserRepository.layer)
  def run: Task[Unit] = program.provide(
    QuillUserRepository.layer,
    DataService.layer,
    Quill.Mysql.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("testMysqlDB")
  )

end UserServer
