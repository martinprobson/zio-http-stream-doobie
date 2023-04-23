package net.martinprobson.example.zio.server

import net.martinprobson.example.zio.ZIOTestApplication
import net.martinprobson.example.zio.common.{Email, User, UserName}
import net.martinprobson.example.zio.repository.InMemoryUserRepository
import net.martinprobson.example.zio.server.UserAppTest.{suiteAll, test}
import zio.ZIO
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.Assertion.*

object UserAppTest extends ZIOTestApplication:

  private val app = UserApp().withDefaultErrorResponse

  def spec = suiteAll("UserAppTest") {
    val users = Range(1, 20).inclusive.toList
      .map { i => User(UserName(s"User-$i"), Email(s"email-$i")) }

    val user = users.head

    test("addUser") {
      val path = !! / "user"
      val req =
        Request.post(body = Body.fromString(user.toJson), url = URL(path))
      for actualBody <- app(req).flatMap(_.body.asString)
      yield assertTrue(actualBody.fromJson[User] == Right(user.copy(id = 1)))
    }

    test("addUsers") {
      val path = !! / "users"
      val req =
        Request.post(body = Body.fromString(users.toJson), url = URL(path))
      for actualBody <- app(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[List[User]])(
        isRight(hasSize(equalTo(users.size)))
      )
    }

    test("getUsers - empty") {
      val path = !! / "users"
      app(Request.get(url = URL(path)))
        .flatMap(_.body.asString)
        .flatMap(body =>
          assert(body.fromJson[List[User]])(isRight(hasSize(equalTo(0))))
        )
    }

    test("getUsers - not empty") {
      val path = !! / "users"
      val req = Request.get(url = URL(path))
      val addUsersReq =
        Request.post(body = Body.fromString(users.toJson), url = URL(path))
      for
        _ <- app(addUsersReq)
        actualBody <- app(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[List[User]])(
        isRight(hasSize(equalTo(users.size)))
      )
    }

    test("getUser - (user exists)") {
      val path = !! / "user" / "4"
      val req = Request.get(url = URL(path))
      val addUsersReq =
        Request.post(
          body = Body.fromString(users.toJson),
          url = URL(!! / "users")
        )
      for
        _ <- app(addUsersReq)
        actualBody <- app(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[User])(
        isRight(hasField("id", _.id, equalTo(4)))
      )
    }

    test("getUser - (user does not exist)") {
      val path = !! / "user" / "4"
      val req = Request.get(url = URL(path))
      for resp <- app(req)
      yield assertTrue(resp.status == Status.NotFound)
    }

    test("count users - (empty database)") {
      val path = !! / "users" / "count"
      val req = Request.get(url = URL(path))
      for actualBody <- app(req).flatMap(_.body.asString)
      yield assert(actualBody)(equalTo("0"))
    }

    test("count users - (non-empty database)") {
      val path = !! / "users" / "count"
      val req = Request.get(url = URL(path))
      val addUsersReq =
        Request.post(
          body = Body.fromString(users.toJson),
          url = URL(!! / "users")
        )
      for
        _ <- app(addUsersReq)
        actualBody <- app(req).flatMap(_.body.asString)
      yield assert(actualBody)(equalTo(users.size.toString))
    }
//TODO test for count
  }.provide(InMemoryUserRepository.layer)
