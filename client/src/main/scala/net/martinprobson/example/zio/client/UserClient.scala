package net.martinprobson.example.zio.client

import net.martinprobson.example.zio.common.User
import zio.http.Response
import zio.{Task, ZIO}

trait UserClient:
  def addUser(user: User): Task[Response]

object UserClient:
  def addUser(user: User): ZIO[UserClient, Throwable, Response] =
    ZIO.serviceWithZIO[UserClient](_.addUser(user))
