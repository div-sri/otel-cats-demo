package demo

import cats.effect.{IO, IOApp, Resource}
import com.zaxxer.hikari.HikariConfig
import demo.AppConfig.{DbConfig, HttpConfig}
import demo.products.{Controller, RepositoryImpl, Service}
import demo.swagger.SwaggerController
import doobie.otel4s.hikari.TelemetryHikariTransactor
import org.flywaydb.core.Flyway
import org.http4s.HttpApp
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.otel4s.middleware.trace.client.UriRedactor
import org.http4s.otel4s.middleware.trace.redact.HeaderRedactor
import org.http4s.otel4s.middleware.trace.server.{ServerSpanDataProvider, ServerMiddleware as OtelServerMiddleware}
import org.http4s.server.Router
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.{IOLocalContextStorage, Context as OtelContext}
import org.typelevel.otel4s.trace.{Tracer, TracerProvider}

import javax.sql.DataSource

object App extends IOApp.Simple {

  override def run: IO[Unit] =
    (for {
      config <- AppConfig.load[IO].toResource

      given LocalProvider[IO, OtelContext] = IOLocalContextStorage.localProvider[IO]

      otelJava <- OtelJava.global[IO].toResource
      tracerProvider = otelJava.tracerProvider
      tracer <- tracerProvider.get("product-service").toResource

      given TracerProvider[IO] = tracerProvider
      given Tracer[IO] = tracer

      dbTransactor <- initDbTransactor(config.dbConfig, otelJava).evalTap(transactor => migrate(transactor.kernel))

      repository = new RepositoryImpl[IO](dbTransactor)
      service = new Service[IO](repository)
      controller = new Controller[IO](service)

      swaggerController = new SwaggerController[IO]

      httpRoutes = Router(
        "/" -> swaggerController.routes,
        "/products" -> controller.routes
      )

      httpApp <- withOtelTracing(httpRoutes.orNotFound)

      _ <- initHttpServer(config.httpConfig, httpApp)
    } yield ()).use(_ => IO.never)

  private def initHttpServer(config: HttpConfig, app: HttpApp[IO]) =
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(app)
      .build

  private def initDbTransactor(config: DbConfig, otelJava: OtelJava[IO]) = {
    val hikariConfig = new HikariConfig() {
      setDriverClassName(config.driver)
      setJdbcUrl(config.url)
      setUsername(config.username)
      setPassword(config.password)
    }

    TelemetryHikariTransactor.fromHikariConfig[IO](otelJava.underlying, hikariConfig)
  }

  private def withOtelTracing(app: HttpApp[IO])(using TracerProvider[IO]) =
    Resource.eval(
      OtelServerMiddleware
        .builder[IO](
          ServerSpanDataProvider
            .openTelemetry(new UriRedactor.OnlyRedactUserInfo {})
            .optIntoHttpRequestHeaders(HeaderRedactor.default)
            .optIntoHttpResponseHeaders(HeaderRedactor.default)
            .optIntoClientPort
        ).build.map(_.wrapHttpApp(app))
    )

  private def migrate(dataSource: DataSource) =
    IO.blocking(
      Flyway
        .configure()
        .baselineOnMigrate(true)
        .schemas("products")
        .locations("classpath:migrations")
        .createSchemas(true)
        .dataSource(dataSource)
        .load()
        .migrate()
    ).void
}
