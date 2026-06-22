package demo

import cats.effect.Sync
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.typesafe.config.{Config, ConfigFactory}
import demo.AppConfig.*

class AppConfig(
  val httpConfig: HttpConfig,
  val dbConfig: DbConfig
)

object AppConfig {

  class HttpConfig(val host: Host, val port: Port)

  class DbConfig(val driver: String, val url: String, val username: String, val password: String)

  def load[F[_] : Sync]: F[AppConfig] = new ConfigParser[F](ConfigFactory.load()).parse()

  private class ConfigParser[F[_] : {Sync as S}](config: Config) {

    def parse(): F[AppConfig] =
      (
        parseHttpConfig(),
        parseDbConfig()
      ).mapN(new AppConfig(_, _))

    private def parseHttpConfig(): F[HttpConfig] =
      (
        S.catchNonFatal(config.getString("http.host")).flatMap(host => S.fromOption(Host.fromString(host), Exception(s"invalid host: $host"))),
        S.catchNonFatal(config.getInt("http.port")).flatMap(port => S.fromOption(Port.fromInt(port), Exception(s"invalid port: $port"))),
      ).mapN(new HttpConfig(_, _))

    private def parseDbConfig(): F[DbConfig] =
      (
        S.catchNonFatal(config.getString("db.driver")),
        S.catchNonFatal(config.getString("db.url")),
        S.catchNonFatal(config.getString("db.username")),
        S.catchNonFatal(config.getString("db.password"))
      ).mapN(new DbConfig(_, _, _, _))
  }
}
