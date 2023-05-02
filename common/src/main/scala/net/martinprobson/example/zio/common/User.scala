package net.martinprobson.example.zio.common

import zio.json.*
import User.*
//import io.getquill.MappedEncoding

type USER_ID = Long
type UserName = String
type Email = String
case class User(
    id: USER_ID = UNASSIGNED_USER_ID,
    name: UserName,
    email: Email
)

object User:

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

end User

//case class UserName(name: String)
//
//object UserName:
//  given decoder: JsonDecoder[UserName] =
//    JsonDecoder[String].map(UserName(_))
//  given encoder: JsonEncoder[UserName] = JsonEncoder[String].contramap(_.name)
//
//  //TODO FIX
//  implicit val encode: MappedEncoding[UserName, String] = MappedEncoding[UserName,String](_.toString)
//  //TODO FIX
//  implicit val decode: MappedEncoding[String, UserName] = MappedEncoding[String,UserName](UserName(_))
//
//case class Email(email: String)
//
//object Email:
//  given decoder: JsonDecoder[Email] = JsonDecoder[String].map(Email(_))
//  given encoder: JsonEncoder[Email] = JsonEncoder[String].contramap(_.email)
//
//  //TODO FIX
//  implicit val encode: MappedEncoding[Email, String] = MappedEncoding[Email, String](_.toString)
//  //TODO FIX
//  implicit val decode: MappedEncoding[String, Email] = MappedEncoding[String, Email](Email(_))
