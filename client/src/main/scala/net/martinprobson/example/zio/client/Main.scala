package net.martinprobson.example.zio.client

import net.martinprobson.example.zio.common.{
  Email,
  MemorySource,
  Source,
  USER_ID,
  User,
  UserName,
  ZIOApplication
}
import zio.{Scope, Task, URIO, ZIO}
import zio.http.*
import zio.stream.*
import zio.connect.file.*
import User.*
import net.martinprobson.example.zio.files.FileSource

object Main extends ZIOApplication:

  private val program: ZIO[UserClient & Source, Throwable, Unit] =
    // FileSource.stream.mapZIOParUnordered(4)(user => UserClient.addUser(user)).runDrain
    Source.stream
      .mapZIOParUnordered(Int.MaxValue)(user => UserClient.addUser(user))
      .runDrain

  override def run: Task[Unit] = program.provide(
    UserClientLive.layer,
    Client.default,
    MemorySource(50000).layer
  )
  //    fileConnectorLiveLayer,
  //    Scope.default)

end Main
