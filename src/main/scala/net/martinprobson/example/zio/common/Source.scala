package net.martinprobson.example.zio.common

import zio.{ZIO, ZLayer}
import zio.stream.{Stream, ZStream}

trait Source:
  def stream: Stream[Throwable, User]

object Source:
  def stream: ZStream[Source, Throwable, User] =
    ZStream.service[Source].flatMap(_.stream)

case class MemorySource(private val size: Int) extends Source:
  val layer: ZLayer[Any, Throwable, MemorySource] =
    ZLayer.succeed(MemorySource(size))

  override def stream: Stream[Throwable, User] = ZStream
    .iterate(1)(_ + 1)
    .map(n => User(s"Username-$n", s"email-$n"))
    .take(size)
