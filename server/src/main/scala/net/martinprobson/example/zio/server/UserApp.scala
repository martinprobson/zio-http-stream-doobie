package net.martinprobson.example.zio.server

import zio.*
import zio.http.*
//import zio.http.model.*
import zio.json.*

import net.martinprobson.example.zio.common.{User, USER_ID}
import net.martinprobson.example.zio.repository.UserRepository

object UserApp:

  def apply(): HttpApp[UserRepository, Throwable] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "users" =>
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
      case req @ Method.POST -> !! / "user" =>
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

      case req @ Method.GET -> !! / "users" =>
        UserRepository.getUsers.map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> !! / "user" / id =>
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
                .map(user =>
                  user match
                    case Some(u) => Response.text(u.toJsonPretty)
                    case None    => Response(status = Status.NotFound)
                )
        yield r

      case req@Method.GET -> !! / "users" / "paged" / pageNo / pageSize =>
        UserRepository
          .getUsersPaged(pageNo.toInt,pageSize.toInt)
          .map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> !! / "user" / "name" / name =>
        UserRepository
          .getUserByName(name)
          .map(users => Response.text(users.toJsonPretty))

      case req @ Method.GET -> !! / "users" / "count" =>
        UserRepository.countUsers
          .map(count => Response.text(count.toString))
    }

end UserApp
