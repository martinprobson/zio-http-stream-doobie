package net.martinprobson.example.zio.repository


import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import zio.{Task, ULayer, ZIO, ZLayer}
import zio.interop.catz.*
import doobie.*
import fs2.Stream
import doobie.free.connection.ConnectionOp
import doobie.free.connection
import doobie.implicits.*
import net.martinprobson.example.zio.common.{USER_ID, User}
import zio.stream.ZStream

class DoobieUserRepository(xa: Transactor[Task]) extends UserRepository {

  def log: SelfAwareStructuredLogger[Task] = Slf4jLogger.getLogger[Task]

  private def insert(user: User): Task[User] = (for
    _ <- sql"INSERT INTO user (name, email) VALUES (${user.name},${user.email})".update.run
    id <- sql"SELECT last_insert_id()".query[Long].unique
    user <- doobie.free.connection.pure(User(id, user.name, user.email))
  yield user).transact(xa)

  private def select(id: USER_ID): ConnectionIO[Option[User]] =
    sql"SELECT id, name, email FROM user WHERE id = $id".query[User].option

  private def selectCount: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM user".query[Long].unique

  private def selectAll: Stream[Task, User] =
    sql"SELECT id, name, email FROM user".query[User].stream.transact(xa)

  private def selectByName(name: String): ConnectionIO[List[User]] =
    sql"SELECT id, name, email FROM user WHERE name = $name"
      .query[User]
      .stream
      .compile
      .toList

  private def selectPaged(pageNo: Int, pageSize: Int): ConnectionIO[List[User]] = {
    val offset = pageNo * pageSize
    sql"SELECT id, name, email FROM user ORDER BY id LIMIT $pageSize OFFSET $offset"
      .query[User]
      .stream
      .compile
      .toList
  }

  override def addUser(user: User): Task[User] = for {
    _ <- log.info(s"About to create : $user")
    user <- insert(user)
    _ <- log.info(s"Created user: $user")
  } yield user

  override def addUsers(users: List[User]): Task[List[User]] = ZIO.foreach(users)(addUser)

  override def getUser(id: USER_ID): Task[Option[User]] = for {
    _ <- log.info(s"getUser: looking for USER_ID = $id")
    o <- select(id).transact(xa)
    _ <- log.info(s"getUser: result is $o")
  } yield o

  override def getUsersPaged(pageNo: Int, pageSize: Int): Task[List[User]] = for {
    _ <- log.info(s"getUsersPaged: pageNo: $pageNo pageSize: $pageSize")
    users <- selectPaged(pageNo, pageSize).transact(xa)
    _ <- log.info(s"getUsersPaged: return $users")
  } yield users

  override def getUserByName(name: String): Task[List[User]] = selectByName(name).transact(xa)

  override def getUsers: Task[List[User]] = selectAll.compile.toList

  override def countUsers: Task[Long] = selectCount.transact(xa)

  def createTable: Task[Int] =
    sql"""
         |create table if not exists user
         |(
         |    id   int auto_increment
         |        primary key,
         |    name  varchar(100) null,
         |    email varchar(100) null
         |         );
         |""".stripMargin.update.run.transact(xa)
}

object DoobieUserRepository {
  val layer: ZLayer[Transactor[Task], Throwable, UserRepository] =
    ZLayer {
      for {
        xa <- ZIO.service[Transactor[Task]]
        u  <- ZIO.attempt(DoobieUserRepository(xa))
        _ <- u.createTable
      } yield u
    }
}
