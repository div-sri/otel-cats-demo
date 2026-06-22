package demo.swagger

import cats.effect.Async
import demo.products.model.*
import org.http4s.HttpRoutes
import sttp.apispec.openapi.Info
import sttp.model.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.UUID

class SwaggerController[F[_] : Async] {

  private val Tag = "/products"
  private val Path = "products"

  val routes: HttpRoutes[F] =
    Http4sServerInterpreter[F]()
      .toRoutes(
        SwaggerInterpreter(
          swaggerUIOptions = SwaggerUIOptions.default.copy(pathPrefix = List())
        ).fromEndpoints(endpoints, info)
      )

  private def info =
    Info(
      title = "Products Service",
      version = "local"
    )

  private def endpoints =
    List(
      endpoint
        .post
        .tag(Tag)
        .in(Path)
        .in(jsonBody[CreateNewProduct])
        .out(jsonBody[String])
        .errorOut(oneOf(
          oneOfVariant(StatusCode.BadRequest, jsonBody[String]),
          oneOfVariant(statusCode(StatusCode.Conflict).description("duplicate product name"))
        )),
      endpoint
        .get
        .tag(Tag)
        .in(Path / path[ProductId]("id") / "info")
        .out(jsonBody[ProductInfo])
        .errorOut(statusCode(StatusCode.NotFound).description("Not Found")),
      endpoint
        .put
        .tag(Tag)
        .in(Path / path[ProductId]("productId") / "quantity")
        .in(jsonBody[UpdateProductQuantityRequest])
        .out(statusCode(StatusCode.NoContent).description("Updated"))
        .errorOut(oneOf(
          oneOfVariant(StatusCode.BadRequest, jsonBody[String]),
          oneOfVariant(statusCode(StatusCode.Conflict).description("invalid product id"))
        )),
      endpoint
        .get
        .tag(Tag)
        .in(Path / path[ProductId]("id") / "quantity")
        .out(jsonBody[ProductQuantity])
        .errorOut(statusCode(StatusCode.NotFound).description("Not Found")),
      endpoint
        .put
        .tag(Tag)
        .in(Path / path[ProductId]("productId") / "price")
        .in(jsonBody[UpdateProductPriceRequest])
        .out(statusCode(StatusCode.NoContent).description("Updated"))
        .errorOut(oneOf(
          oneOfVariant(StatusCode.BadRequest, jsonBody[String]),
          oneOfVariant(statusCode(StatusCode.Conflict).description("invalid product id"))
        )),
      endpoint
        .get
        .tag(Tag)
        .in(Path / path[ProductId]("id") / "price")
        .out(jsonBody[ProductPrice])
        .errorOut(statusCode(StatusCode.NotFound).description("Not Found"))
    )
}
