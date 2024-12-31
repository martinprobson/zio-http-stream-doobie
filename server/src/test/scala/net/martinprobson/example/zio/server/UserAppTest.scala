package net.martinprobson.example.zio.server

import net.martinprobson.example.zio.ZIOTestApplication
import net.martinprobson.example.zio.common.User
import net.martinprobson.example.zio.repository.InMemoryUserRepository
import net.martinprobson.example.zio.server.UserAppTest.{suiteAll, test}
import zio.{Scope, ZIO}
import zio.http.*
import zio.json.*
import zio.test.*
import zio.test.Assertion.*

object UserAppTest extends ZIOTestApplication:

  private val app = UserApp.routes.handleError(_ => Response.error(Status.InternalServerError))

  def spec: Spec[TestEnvironment with Scope, Any] = suiteAll("UserAppTest") {
    val users = Range(1, 20).inclusive.toList
      .map { i => User(s"User-$i", s"email-$i") }

    val user = users.head

    test("addUser") {
      val req = Request.post(toURL("user"), Body.fromString(user.toJson))
      for 
        actualBody <- app.runZIO(req).flatMap(_.body.asString)
        _ <- ZIO.logInfo(s"actualBody = $actualBody")
      yield assertTrue(actualBody.fromJson[User] == Right(user.copy(id = 1)))
    }

    test("addUsers") {
      val req = Request.post(toURL("users"), Body.fromString(users.toJson))
      for actualBody <- app.runZIO(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[List[User]])(
        isRight(hasSize(equalTo(users.size)))
      )
    }

    test("getUsers - empty") {
      app
        .runZIO(Request.get(toURL("users")))
        .flatMap(_.body.asString)
        .flatMap(body =>
          assert(body.fromJson[List[User]])(isRight(hasSize(equalTo(0))))
        )
    }

    test("getUsers - not empty") {
      val req = Request.get(toURL("users"))
      val addUsersReq =
        Request.post(toURL("users"), Body.fromString(users.toJson))
      for
        _ <- app.runZIO(addUsersReq)
        actualBody <- app.runZIO(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[List[User]])(
        isRight(hasSize(equalTo(users.size)))
      )
    }

    test("getUser - (user exists)") {
      val req = Request.get(toURL("user/4"))
      val addUsersReq =
        Request.post(
          toURL("users"),
          Body.fromString(users.toJson)
        )
      for
        _ <- app.runZIO(addUsersReq)
        actualBody <- app.runZIO(req).flatMap(_.body.asString)
      yield assert(actualBody.fromJson[User])(
        isRight(hasField("id", _.id, equalTo(4)))
      )
    }

    test("getUser - (user does not exist)") {
      val req = Request.get(toURL("user/4"))
      for resp <- app.runZIO(req)
      yield assertTrue(resp.status == Status.NotFound)
    }

    test("count users - (empty database)") {
      val req = Request.get(toURL("users/count"))
      for actualBody <- app.runZIO(req).flatMap(_.body.asString)
      yield assertTrue(actualBody == "0")
    }

    test("count users - (non-empty database)") {
      val req = Request.get(toURL("users/count"))
      val addUsersReq =
        Request.post(
          toURL("users"),
          Body.fromString(users.toJson)
        )
      for
        _ <- app.runZIO(addUsersReq)
        actualBody <- app.runZIO(req).flatMap(_.body.asString)
      yield assertTrue(actualBody == users.size.toString)
    }
  }.provide(InMemoryUserRepository.layer)
  
  private def toURL(url: String): URL = URL.decode(url).getOrElse(URL.empty)

end UserAppTest

