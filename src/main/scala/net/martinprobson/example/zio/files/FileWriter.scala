package net.martinprobson.example.zio.files

import zio.*
import zio.stream.*
import zio.connect.file.*
import zio.json.*

import java.io.{File, IOException}
import java.nio.file.Path

import net.martinprobson.example.zio.common.{
  ZIOApplication,
  User,
  UserName,
  Email
}

object FileWriter extends ZIOApplication:

  /** Generate user files.
    * @param fileIndex
    *   The number of files to generate
    * @param numOfLines
    *   The number of Users per file
    */
  def generateUserFile(
      fileIndex: Int,
      numOfLines: Long
  ): ZIO[FileConnector, IOException, Unit] =
    ZStream
      .iterate(((fileIndex - 1) * numOfLines) + 1)(_ + 1)
      .map(n => User(UserName(s"Username-$n"), Email(s"email-$n")))
      .map(u => u.toJson)
      .take(numOfLines)
      .tap(res => ZIO.logInfo(res.toString))
      .via(ZPipeline.intersperse("\n"))
      .via(ZPipeline.utf8Encode)
      .run(
        writeFileName(
          s"${java.lang.System.getProperty("java.io.tmpdir")}/test_file_$fileIndex.txt"
        )
      )

  /** Generate user files. Each file is generated in parallel (using
    * <code>parTraverse</code>.
    * @param numFiles
    *   The number of files to generate
    * @param numOfLines
    *   The number of Users per file
    */
  def generateUserFiles(
      numFiles: Int,
      numOfLines: Long
  ): ZIO[FileConnector, IOException, Unit] =
    ZIO
      .foreachPar(1 to numFiles) { i =>
        generateUserFile(i, numOfLines) *> ZIO.logInfo("Done")
      }
      .unit

  private val program: ZIO[FileConnector, IOException, Unit] =
    // generateUserFile(1, 100).run(ZSink.head)
    generateUserFiles(20, 1000)

  override def run: Task[Unit] =
    program.provide(fileConnectorLiveLayer) *> ZIO.logInfo("Done")

end FileWriter
