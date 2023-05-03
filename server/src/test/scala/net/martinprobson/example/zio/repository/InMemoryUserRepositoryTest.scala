package net.martinprobson.example.zio.repository

import zio.{Scope, ZIO}
import zio.test.*
import net.martinprobson.example.zio.ZIOTestApplication
import net.martinprobson.example.zio.common.User
import net.martinprobson.example.zio.repository.{InMemoryUserRepository, UserRepository}

object InMemoryUserRepositoryTest extends ZIOTestApplication:

  def spec: Spec[TestEnvironment with Scope, Any] = suiteAll("InMemoryRepositoryTest") {
    val users = Range(1, 20).inclusive.toList
      .map { i => User(s"User-$i", s"email-$i") }

    val user = users.head

    test("countUsers (empty database)") {
      UserRepository.countUsers.flatMap { c => assertTrue(c == 0) }
    }
    test("countUsers (non-empty database)") {
      for
        _ <- ZIO.foreachParDiscard(users)(user => UserRepository.addUser(user))
        c <- UserRepository.countUsers
      yield assertTrue(c == users.size)
    }
    test("addUser") {
      for
        u <- UserRepository.addUser(user)
        c <- UserRepository.countUsers
      yield assertTrue(
        c == 1 && u.name == "User-1" && u.email == "email-1"
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
        u <- UserRepository.getUserByName("User-5")
      yield assertTrue(u.size == 1 && u.head.name == "User-5")
    }
    test("getUserByName (> 1 match)") {
      for
        _ <- UserRepository.addUsers(users)
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUserByName("User-5")
      yield assertTrue(u.count(u => u.name == "User-5") == 2)
    }
    test("getUserByName (no match)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUserByName("zzzz")
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
        u <- UserRepository.getUserByName("User-6")
      yield assertTrue(u.size == 1 && u.head.name == "User-6")
    }
    test("getUser (user does not exist)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUser(9999)
      yield assertTrue(u.isEmpty)
    }
    test("getUsersPaged (non empty database)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUsersPaged(0,2)
      yield assertTrue(u.size == 2 && u.head.id == 1 && u.tail.head.id == 2)
    }
    test("getUsersPaged (empty database)") {
      UserRepository.getUsersPaged(0,2).flatMap(u => assertTrue(u.isEmpty))
    }
    test("getUsersPaged (page size bigger than users in db)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUsersPaged(0, 2000)
      yield assertTrue(u.size == 20)
    }
    test("getUsersPaged (page number bigger than users in db)") {
      for
        _ <- UserRepository.addUsers(users)
        u <- UserRepository.getUsersPaged(20000, 1)
      yield assertTrue(u.isEmpty)
    }
  }.provide(InMemoryUserRepository.layer)
