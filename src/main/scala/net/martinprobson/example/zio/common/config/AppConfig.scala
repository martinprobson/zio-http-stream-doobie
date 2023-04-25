package net.martinprobson.example.zio.common.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

final case class AppConfig(
                                    threads: Int,
                                    driverClassName: String,
                                    url: String,
                                    user: String,
                                    password: String,
                                    host: String,
                                    port: Int
                                  )

object AppConfig:
  val config: Config[AppConfig] = deriveConfig[AppConfig].nested("ApplicationConfig")
