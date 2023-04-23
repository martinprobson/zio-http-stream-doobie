package net.martinprobson.example.zio.repository


import zio.ZIO
import zio.test.*
import net.martinprobson.example.zio.ZIOTestApplication
import net.martinprobson.example.zio.common.{Email, User, UserName}
import net.martinprobson.example.zio.repository.{InMemoryUserRepository, UserRepository}

object InMemoryRepositoryTest extends ZIOTestApplication:

  def spec = suiteAll("InMemoryRepositoryTest") {
    val users = Range(1, 20).inclusive.toList
      .map { i => User(UserName(s"User-$i"), Email(s"email-$i")) }

    val user = users.head

    test("countUsers (empty database)") {
      UserRepository.countUsers.flatMap { c => assertTrue(c == 0) }
    }
    test("countUsers (non-empty database)") {
      for
        _ <- ZIO.foreachPar(users)(user => UserRepository.addUser(user))
        c <- UserRepository.countUsers
      yield assertTrue(c == users.size)
    }
    test("addUser") {
      for
        u <- UserRepository.addUser(user)
        c <- UserRepository.countUsers
      yield assertTrue(
        c == 1 && u.name == UserName("User-1") && u.email == Email("email-1")
      )
    }
    test("addUsers") {
      for
        _ <- UserRepository.addUsers(users)
        c <- UserRepository.countUsers
      yield assertTrue(c == users.size)
    }
    test("getUserByName (1 match)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUserByName(UserName("User-5"))
      yield assertTrue(u.size == 1 && u.head.name == UserName("User-5"))
    }
    test("getUserByName (> 1 match)") {
      for
        _ <- UserRepository.addUsers(users)
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUserByName(UserName("User-5"))
      yield assertTrue(u.filter(u => u.name == UserName("User-5")).size == 2)
    }
    test("getUserByName (no match)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUserByName(UserName("zzzz"))
      yield assertTrue(u.isEmpty)
    }
    test("getUsers (non-empty database)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUsers
      yield assertTrue(u.size == users.size)
    }
    test("getUsers (empty database)") {
      UserRepository.getUsers.flatMap(u => assertTrue(u.isEmpty))
    }
    test("getUser (user exists)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUser(6)
      yield assertTrue(u == Some(User(6, UserName("User-6"), Email("email-6"))))
    }
    test("getUser (user does not exist)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUser(9999)
      yield assertTrue(u == None)
    }
  }.provide(InMemoryUserRepository.layer)
