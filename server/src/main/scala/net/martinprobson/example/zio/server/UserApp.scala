package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
import zio.json.*
import net.martinprobson.example.zio.common.{USER_ID, User}
import net.martinprobson.example.zio.repository.UserRepository
import zio.http.endpoint.Endpoint
import zio.stream.ZStream

object UserApp:

  val routes: Routes[UserRepository, Throwable] = Routes(
    Method.POST / "users" -> handler { (req: Request) =>
      for
        users <- req.body.asString.map(_.fromJson[List[User]])
        _ <- ZIO.logInfo(s"user = $users")
        response <- users match
          case Left(e) =>
            ZIO
              .logError(s"Failed to parse input: $e")
              .as(Response.text(e).status(Status.BadRequest))
          case Right(users) =>
            UserRepository
              .addUsers(users)
              .map(users => Response.text(users.toJson))
      yield response
    },
    Method.POST / "user" -> handler { (req: Request) =>
      for
        u <- req.body.asString.map(_.fromJson[User])
        r <- u match
          case Left(e) =>
            ZIO
              .logError(s"Failed to parse the input: $e")
              .as(Response.text(e).status(Status.BadRequest))
          case Right(u) =>
            UserRepository
              .addUser(u)
              .map(user => Response.text(user.toJson))
      yield r
    },
    Method.GET / "users" -> handler { (_: Request) =>
      UserRepository.getUsers.map(users => Response.text(users.toJsonPretty))
    },
    Method.GET / "user" / string("id") -> handler { (id: String, _: Request) =>
      for
        i <- ZIO.attempt(id.toLong).either
        r <- i match
          case Left(e) =>
            ZIO
              .debug(s"Failed to parse the input: $e")
              .as(Response.text(e.toString).status(Status.BadRequest))
          case Right(u) =>
            UserRepository
              .getUser(u)
              .map {
                case Some(u) => Response.text(u.toJsonPretty)
                case None    => Response(status = Status.NotFound)
              }
      yield r
    },
    Method.GET / "users" / "count" -> handler { (_: Request) =>
      UserRepository.countUsers.map(count => Response.text(count.toString))
    },
    Method.GET / "user" / "name" / string("name") -> handler {
      (name: String, _: Request) =>
        UserRepository
          .getUserByName(name)
          .map(users => Response.text(users.toJsonPretty))
    },
    Method.GET / "users" / "paged" / int("pageNo") / int(
      "pageSize"
    ) -> handler { (pageNo: Int, pageSize: Int, _: Request) =>
      UserRepository
        .getUsersPaged(pageNo, pageSize)
        .map(users => Response.text(users.toJsonPretty))
    },
    Method.GET / "seconds" -> handler { (_: Request) =>
      ZIO.logInfo("seconds").as {
        Response(
          body =
            Body.fromCharSequenceStreamChunked(InfiniteStream.stream.take(10))
        )
      }
    },
    Method.GET / "hello" -> handler { (_: Request) =>
      ZIO.logInfo("In hello").as(Response.text("Hello world"))
    }
  )

end UserApp
