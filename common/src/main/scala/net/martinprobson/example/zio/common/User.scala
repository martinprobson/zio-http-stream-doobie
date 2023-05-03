package net.martinprobson.example.zio.common

import zio.json.*
import User.*

type USER_ID = Long
type UserName = String
type Email = String
case class User(
    id: USER_ID = UNASSIGNED_USER_ID,
    name: UserName,
    email: Email
)

object User {

  given decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]

  given encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  def apply(
             id: USER_ID,
             name: UserName,
             email: Email
           ): User =
    new User(id, name, email)

  def apply(name: UserName, email: Email): User =
    User(UNASSIGNED_USER_ID, name, email)

  val UNASSIGNED_USER_ID = 0L

}