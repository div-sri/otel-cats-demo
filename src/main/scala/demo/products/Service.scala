package demo.products

import cats.effect.Sync
import cats.effect.std.SecureRandom
import cats.syntax.all.*
import demo.products.model.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.trace.Tracer

import java.time.Instant

class Service[F[_] : {Sync, SecureRandom, Tracer}](repository: Repository[F]) {

  private val logger = Slf4jLogger.getLoggerFromClass[F](getClass)

  def fetchProductInfo(id: ProductId): F[Option[ProductInfo]] =
    Tracer[F].span("Service.fetchProductInfo").surround(
      logger.info(s"fetching product info for product-id: $id") *>
        repository.fetchProductInfo(id) <*
        logger.info(s"fetched product info for product-id: $id")
    )

  def fetchProductQuantity(id: ProductId): F[Option[ProductQuantity]] =
    Tracer[F].span("Service.fetchProductQuantity").surround(
      logger.info(s"fetching product quantity for product-id: $id") *>
        repository.fetchProductQuantity(id) <*
        logger.info(s"fetched product quantity for product-id: $id")
    )

  def fetchProductPrice(id: ProductId): F[Option[ProductPrice]] =
    Tracer[F].span("Service.fetchProductPrice").surround(
      logger.info(s"fetching product price for product-id: $id") *>
        repository.fetchProductPrice(id) <*
        logger.info(s"fetched product price for product-id: $id")
    )

  def createNewProduct(request: CreateNewProduct): F[ProductId] =
    Tracer[F].span("Service.createNewProduct").surround(
      for {
        _ <- logger.info("adding new product info")
        productId <- ProductId.gen[F]

        productInfo = ProductInfo(
          id = productId,
          name = request.name,
          description = request.description,
          createdAt = Instant.now(),
          isActive = true
        )

        _ <- repository.insertProductInfo(productInfo)
        _ <- logger.info(s"added new product with id: $productId")
      } yield productId
    )

  def updateProductQuantity(id: ProductId, req: UpdateProductQuantityRequest): F[Unit] =
    Tracer[F].span("Service.updateProductQuantity").surround(
      logger.info(s"updating product quantity for product-id: $id") *>
        repository.updateProductQuantity(
          ProductQuantity(
            productId = id, quantity = req.quantity, unit = req.unit
          )
        ) *>
        logger.info(s"updated product quantity for product-id: $id")
    )

  def updateProductPrice(id: ProductId, req: UpdateProductPriceRequest): F[Unit] =
    Tracer[F].span("Service.updateProductPrice").surround(
      logger.info(s"updating product price for product-id: $id") *>
        repository.updateProductPrice(
          ProductPrice(
            productId = id, price = req.price, currency = req.currency
          )
        ) *>
        logger.info(s"updating product price for product-id: $id")
    )
}
