package net.martinprobson.example.zio.client

import net.martinprobson.example.zio.common.User
import zio.*
import zio.json.*
import zio.http.*
import zio.http.model.{Headers, Method}

case class UserClientLive (client: Client) extends UserClient:
  override def addUser(user: User): Task[Response] = for
    _ <- ZIO.logInfo(s"In addUser - user = $user")
    resp <- client.request(Request.post(Body.fromString(user.toJson),
      url = URL.fromString("http://localhost:8085/user").getOrElse(URL.empty))).retry(Schedule.recurs(3))
  yield resp


object UserClientLive:
  val layer: ZLayer[Client, Throwable, UserClientLive] = ZLayer {
    for
      client      <- ZIO.service[Client]
    yield UserClientLive(client)
  }
