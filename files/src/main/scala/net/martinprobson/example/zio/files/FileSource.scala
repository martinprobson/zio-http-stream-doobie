package net.martinprobson.example.zio.files

import zio.*
import zio.stream.*
import zio.connect.file.*
import zio.json.*

import java.io.{File, IOException}
import java.nio.file.Path
import net.martinprobson.example.zio.common.{Email, Source, User, UserName, ZIOApplication}

object FileSource extends ZIOApplication with Source:

  private val userStream: ZStream[FileConnector, IOException, User] =
    listPath(Path.of("/tmp/"))
      .filter(p => p.getFileName.toString.startsWith("test_file_"))
      .map { path =>
        readPath(path)
          .via(ZPipeline.utfDecode)
          .via(ZPipeline.splitLines)
      }
      .flattenParUnbounded(16)
      .via(
        ZPipeline
          .mapZIOParUnordered(200)(user => {
            user.fromJson[User] match
              case Left(e) =>
                ZIO.fail(new IOException(s"Failed to parse the input: $e"))
              case Right(u) =>
                ZIO.logInfo(s"User: $u") *>
                  ZIO.succeed(u)
          })
      )
//    .mapZIOParUnordered(20)(user => ZIO.fail(new IOException("whoops!")))

  private val program: ZIO[FileConnector, Throwable, Unit] = userStream
    // .tap(res => ZIO.logInfo(res.toString))
    .runDrain

  override def run: Task[Unit] = program.provide(fileConnectorLiveLayer)

  override def stream: Stream[Exception, User] = userStream.provideLayer(fileConnectorLiveLayer)
end FileSource
