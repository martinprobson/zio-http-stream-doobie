package net.martinprobson.example.zio

import zio.{Chunk, Task, UIO, ZIO, ZLayer}
import zio.stream.*

import net.martinprobson.example.zio.common.User
import net.martinprobson.example.zio.common.{
  User,
  ZIOApplication
}

object Main extends ZIOApplication:

  val numbers = 1 to 1000
  val users: ZPipeline[Any, Nothing, Int, User] =
    ZPipeline.map(i => User(s"name-$i", s"email-$i"))
  val take5 = ZSink.take(5)

  val userStream: UIO[Chunk[User]] =
    ZStream.fromIterable(numbers).via(users).run(ZSink.collectAll[User])

  override def run: Task[Unit] =
    for u <- userStream.forEachZIO(u =>
        ZIO.foreachPar(u) { usr => ZIO.logInfo(s"$usr") }
      )
    yield ()

end Main
