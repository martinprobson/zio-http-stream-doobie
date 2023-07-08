package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import zio.json.*
import net.martinprobson.example.zio.common.{USER_ID, User}
import net.martinprobson.example.zio.repository.UserRepository
import zio.stream.ZStream

object UserApp:

  def apply(): HttpApp[UserRepository, Throwable] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> Root / "users" =>
        for
          users <- req.body.asString.map(_.fromJson[List[User]])
          response <- users match
            case Left(e) =>
              ZIO
                .debug(s"Failed to parse input: $e")
                .as(Response.text(e).withStatus(Status.BadRequest))
            case Right(users) =>
              UserRepository
                .addUsers(users)
                .map(users => Response.text(users.toJson))
        yield response
      case req @ Method.POST -> Root / "user" =>
        for
          u <- req.body.asString.map(_.fromJson[User])
          r <- u match
            case Left(e) =>
              ZIO
                .debug(s"Failed to parse the input: $e")
                .as(
                  Response.text(e).withStatus(Status.BadRequest)
                )
            case Right(u) =>
              UserRepository
                .addUser(u)
                .map(user => Response.text(user.toJson))
        yield r

      case req @ Method.GET -> Root / "users" =>
        UserRepository.getUsers.map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> Root / "user" / id =>
        for
          i <- ZIO.attempt(id.toLong).either
          r <- i match
            case Left(e) =>
              ZIO
                .debug(s"Failed to parse the input: $e")
                .as(
                  Response.text(e.toString).withStatus(Status.BadRequest)
                )
            case Right(u) =>
              UserRepository
                .getUser(u)
                .map {
                  case Some(u) => Response.text(u.toJsonPretty)
                  case None    => Response(status = Status.NotFound)
                }
        yield r

      case req @ Method.GET -> Root / "users" / "paged" / pageNo / pageSize =>
        UserRepository
          .getUsersPaged(pageNo.toInt, pageSize.toInt)
          .map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> Root / "user" / "name" / name =>
        UserRepository
          .getUserByName(name)
          .map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> Root / "users" / "count" =>
        UserRepository.countUsers
          .map(count => Response.text(count.toString))

      case req @ Method.GET -> Root / "hello" =>
        ZIO.logInfo("In hello") *> ZIO.succeed(Response.text("Hello world"))

      case req @ Method.GET -> Root / "seconds" =>
        ZIO.logInfo("seconds") *> ZIO.succeed(
          Response(body = Body.fromStream(InfiniteStream.stream.take(10)))
        )

    }

end UserApp
