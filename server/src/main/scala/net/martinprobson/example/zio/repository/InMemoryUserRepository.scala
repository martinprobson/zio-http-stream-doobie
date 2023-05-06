package net.martinprobson.example.zio.repository

import zio.{Ref, Task, ULayer, ZIO, ZLayer}
import zio.ZIOAspect.*
import net.martinprobson.example.zio.common.{USER_ID, User, UserName}
import net.martinprobson.example.zio.repository.UserRepository
import zio.stream.ZStream

import scala.collection.immutable.SortedMap

case class InMemoryUserRepository(
    db: Ref[SortedMap[USER_ID, User]],
    counter: Ref[USER_ID]
) extends UserRepository {

  def addUser(user: User): Task[User] = for
    id <- counter.modify(x => (x + 1, x + 1))
    _ <- ZIO.logInfo(s"About to create User: $user")
    _ <- db.update(users => users.updated(key = id, value = user.copy(id = id)))
    u <- ZIO.succeed(User(id, user.name, user.email))
    _ <- ZIO.logInfo(s"Created User: $u")
  yield u

  def addUsers(users: List[User]): Task[List[User]] =
    ZIO.foreachPar(users)(addUser)

  def getUser(id: USER_ID): Task[Option[User]] =
    db.get.map { users =>
      users.get(key = id).map { user => User(id, user.name, user.email) }
    }

  def countUsers: Task[Long] = ZIO.logInfo("In countUsers") *> db.get.flatMap {
    users => ZIO.attempt(users.size.toLong)
  }

  def getUsers: Task[List[User]] = db.get.map { users =>
    users.map { case (id, user) => User(id, user.name, user.email) }.toList
  }

  def getUserByName(name: UserName): Task[List[User]] = db.get.map { users =>
    users
      .filter { case (_, user) => user.name == name }
      .map { case (id, user) => User(id, user.name, user.email) }
      .toList
  }

  override def getUsersPaged(pageNo: Int, pageSize: Int): Task[List[User]] = db.get.flatMap { users =>
    ZIO.attempt(users.slice(pageNo * pageSize, pageNo * pageSize + pageSize).toList.map { case (_, user) => user })
  }
}

object InMemoryUserRepository {
  val layer: ULayer[UserRepository] =
    ZLayer {
      for
        counter <- Ref.make(User.UNASSIGNED_USER_ID)
        db <- Ref.make(SortedMap.empty[USER_ID, User])
      yield InMemoryUserRepository(db, counter)
    }
}
