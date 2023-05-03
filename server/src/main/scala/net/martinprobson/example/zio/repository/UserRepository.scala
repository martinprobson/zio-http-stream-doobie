package net.martinprobson.example.zio.repository

import net.martinprobson.example.zio.common.{User, USER_ID, UserName}
import zio.{Task, ZIO}
import User.*

trait UserRepository:

  def addUser(user: User): Task[User]
  def addUsers(users: List[User]): Task[List[User]]
  def getUser(id: USER_ID): Task[Option[User]]
  def countUsers: Task[Long]
  def getUserByName(name: UserName): Task[List[User]]
  def getUsers: Task[List[User]]
  //  TODO Add
  //  def getUsersStream: Stream[IO, User]

  def getUsersPaged(pageNo: Int, pageSize: Int): Task[List[User]]
  def getOrAdd(user: User): Task[User] = for {
    userList <- getUserByName(user.name)
    newUser <-
      userList.headOption match {
        case Some(o) => ZIO.succeed(o)
        case None => addUser(user)
      }
  } yield newUser

object UserRepository {
  def addUser(user: User): ZIO[UserRepository, Throwable, User] =
    ZIO.serviceWithZIO[UserRepository](_.addUser(user))

  def addUsers(users: List[User]): ZIO[UserRepository, Throwable, List[User]] =
    ZIO.serviceWithZIO[UserRepository](_.addUsers(users))

  def getUser(id: USER_ID): ZIO[UserRepository, Throwable, Option[User]] =
    ZIO.serviceWithZIO[UserRepository](_.getUser(id))

  def countUsers: ZIO[UserRepository, Throwable, Long] =
    ZIO.serviceWithZIO[UserRepository](_.countUsers)

  def getUserByName(
                     name: UserName
                   ): ZIO[UserRepository, Throwable, List[User]] =
    ZIO.serviceWithZIO[UserRepository](_.getUserByName(name))

  def getUsers: ZIO[UserRepository, Throwable, List[User]] =
    ZIO.serviceWithZIO[UserRepository](_.getUsers)

  def getOrAdd(user: User): ZIO[UserRepository, Throwable, User] =
    ZIO.serviceWithZIO[UserRepository](_.getOrAdd(user))

  def getUsersPaged(pageNo: Int, pageSize: Int): ZIO[UserRepository, Throwable, List[User]] =
    ZIO.serviceWithZIO[UserRepository](_.getUsersPaged(pageNo, pageSize))
}
