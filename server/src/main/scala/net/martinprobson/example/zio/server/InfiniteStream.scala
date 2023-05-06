package net.martinprobson.example.zio.server

import zio.*
import zio.stream.*
import net.martinprobson.example.zio.common.ZIOApplication
import java.util.Date

object InfiniteStream extends ZIOApplication {

  /** Generates an infinite stream that emits the String "Hello" and a timestamp
    * every second.
    */
  val stream: UStream[String] =
    ZStream
      .succeed("Hello")
      .forever
      .zipWithIndex
      .map { case (msg, idx) => s"$msg - $idx" }
      .schedule(Schedule.spaced(1.second))
      .map { msg =>
        {
          val ts = new Date()
          s"$msg - $ts\n"
        } // Add a timestamp to the String
      }
      .tap(x => ZIO.logInfo(s"$x"))

  private def program: Task[Unit] =
    stream.take(10).run(ZSink.drain)

  def run: Task[Unit] = program

}
