package demo.products

import cats.effect.Async
import cats.syntax.all.*
import demo.products.Controller.ProductIdVar
import demo.products.model.{CreateNewProduct, ProductId, UpdateProductPriceRequest, UpdateProductQuantityRequest}
import io.circe.{Encoder, Json}
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware
import org.http4s.{HttpRoutes, Response}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Controller {

  object ProductIdVar {
    def unapply(id: String): Option[ProductId] = ProductId.fromString(id).toOption
  }
}

class Controller[F[_] : Async](service: Service[F]) {

  private val logger = Slf4jLogger.getLoggerFromClass[F](getClass)

  private val dsl = Http4sDsl[F]

  import dsl.*

  val routes: HttpRoutes[F] =
    middleware.Logger.httpRoutes[F](
      logHeaders = false,
      logBody = false,
      logAction = Some(msg => logger.info(msg))
    )(_routes)

  private lazy val _routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request@POST -> Root =>
        request.as[CreateNewProduct]
          .flatMap(service.createNewProduct)
          .flatMap(Ok(_))
          .recoverWith(recoverWith)

      case GET -> Root / ProductIdVar(id) / "info" =>
        service.fetchProductInfo(id).flatMap(okOrNotFound)

      case GET -> Root / ProductIdVar(id) / "quantity" =>
        service.fetchProductQuantity(id).flatMap(okOrNotFound)

      case request@PUT -> Root / ProductIdVar(id) / "quantity" =>
        request.as[UpdateProductQuantityRequest]
          .flatMap(service.updateProductQuantity(id, _))
          .productR(NoContent())
          .recoverWith(recoverWith)

      case GET -> Root / ProductIdVar(id) / "price" =>
        service.fetchProductPrice(id).flatMap(okOrNotFound)

      case request@PUT -> Root / ProductIdVar(id) / "price" =>
        request.as[UpdateProductPriceRequest]
          .flatMap(service.updateProductPrice(id, _))
          .productR(NoContent())
          .recoverWith(recoverWith)
    }

  private def okOrNotFound[B: Encoder](result: Option[B]) =
    result match {
      case Some(result) => Ok(result)
      case None => NotFound()
    }

  private def recoverWith: PartialFunction[Throwable, F[Response[F]]] = {
    case _: java.sql.SQLIntegrityConstraintViolationException => Conflict()
    case ex: org.http4s.DecodeFailure => BadRequest(Json.fromString(ex.message))
  }
}
