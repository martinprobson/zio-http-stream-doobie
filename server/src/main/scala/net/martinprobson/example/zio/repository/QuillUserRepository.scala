package net.martinprobson.example.zio.repository

import io.getquill.{SnakeCase, query}
import io.getquill.jdbczio.Quill
import net.martinprobson.example.zio.common.{USER_ID, User, ZIOApplication}
import zio.{Task, ULayer, ZEnvironment, ZIO, ZLayer}
import io.getquill.*

case class DataService(quill: Quill.Mysql[SnakeCase]):
  import quill.*

  inline def insertUser = quote { (user: User) =>
    users.insertValue(user).returningGenerated(_.id)
  }

  inline def users = quote { query[User] }

  inline def getUsersByName = quote { (name: String) =>
    users.filter(u => u.name == name)
  }

  inline def getUser = quote { (id: USER_ID) => users.filter(u => u.id == id) }

  inline def countUsers = quote { users.size }
end DataService

object DataService:
  val layer = ZLayer.fromFunction(DataService.apply _)

case class QuillUserRepository(dataService: DataService) extends UserRepository:
  import dataService.quill.*

  def addUserInternal(user: User): Task[User] =
    ZIO.logInfo(s"Adding User: $user") *> run(
      dataService.insertUser(lift(user))
    ).flatMap { id => getUser(id) }.map { opt =>
      opt match
        case Some(u) => u
        case None    => User(0, "U", "U")
    }

  override def addUser(user: User): Task[User] =
    //ZIO.blocking { addUserInternal(user) }
   addUserInternal(user)

  override def addUsers(users: List[User]): Task[List[User]] = ???

  override inline def getUser(id: USER_ID): Task[Option[User]] =
    run(dataService.getUser(lift(id))).head.option

  override inline def countUsers: Task[Long] = run(dataService.countUsers)

  override inline def getUserByName(name: String): Task[List[User]] = run(
    dataService.getUsersByName(lift(name))
  )

  override inline def getUsers: Task[List[User]] = run(dataService.users)

end QuillUserRepository

object QuillUserRepository:
  val layer = ZLayer.fromFunction(QuillUserRepository.apply(_))

object Main extends ZIOApplication:
  override def run = {
    UserRepository
      .addUser(User("Martin", "Martin email"))
      .provide(
        QuillUserRepository.layer,
        DataService.layer,
        Quill.Mysql.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("testMysqlDB")
      )
      .debug("Results")
      .exitCode
  }
