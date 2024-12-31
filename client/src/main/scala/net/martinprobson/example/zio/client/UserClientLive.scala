package net.martinprobson.example.zio.client

import net.martinprobson.example.zio.common.User
import net.martinprobson.example.zio.common.config.AppConfig
import zio.*
import zio.json.*
import zio.http.*

case class UserClientLive(client: Client) extends UserClient:
  override def addUser(user: User): Task[Response] = for
    _ <- ZIO.logInfo(s"In addUser - user = $user")
    config <- ZIO.config(AppConfig.config)
    resp <- client
      .batched(
        Request.post(
          url = URL
            .decode(s"http://${config.host}:${config.port}/user")
            .getOrElse(URL.empty),
          Body.fromString(user.toJson)
        )
      )
  yield resp

object UserClientLive:
  val layer: ZLayer[Client, Throwable, UserClient] = ZLayer {
    for client <- ZIO.service[Client]
    yield UserClientLive(client)
  }
