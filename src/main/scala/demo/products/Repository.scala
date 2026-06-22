package demo.products

import cats.effect.Async
import cats.syntax.all.*
import demo.products.model.{ProductId, ProductInfo, ProductPrice, ProductQuantity}
import doobie.*
import doobie.implicits.*
import doobie.implicits.javatimedrivernative.*

trait Repository[F[_]] {
  def fetchProductInfo(id: ProductId): F[Option[ProductInfo]]
  def insertProductInfo(update: ProductInfo): F[Unit]
  def fetchProductQuantity(id: ProductId): F[Option[ProductQuantity]]
  def updateProductQuantity(update: ProductQuantity): F[Unit]
  def fetchProductPrice(id: ProductId): F[Option[ProductPrice]]
  def updateProductPrice(update: ProductPrice): F[Unit]
}

class RepositoryImpl[F[_] : Async](transactor: Transactor[F]) extends Repository[F] {

  override def fetchProductInfo(id: ProductId): F[Option[ProductInfo]] = {
    val q = fr"select * from products.product_info where id = $id"

    q.query[ProductInfo].option.transact(transactor)
  }

  override def insertProductInfo(update: ProductInfo): F[Unit] = {
    val q =
      fr"""
          insert into products.product_info (id, name, description, created_at, is_active)
          values (${update.id}, ${update.name}, ${update.description}, ${update.createdAt}, ${update.isActive})
          """

    q.update.run.transact(transactor).void
  }

  override def fetchProductQuantity(id: ProductId): F[Option[ProductQuantity]] = {
    val q = fr"select * from products.product_quantity where product_id = $id"

    q.query[ProductQuantity].option.transact(transactor)
  }

  override def updateProductQuantity(update: ProductQuantity): F[Unit] = {
    val q =
      fr"""
          insert into products.product_quantity (product_id, quantity, unit)
          values (${update.productId}, ${update.quantity}, ${update.unit})
          on duplicate key update
            quantity = ${update.quantity},
            unit = ${update.unit}
          """

    q.update.run.transact(transactor).void
  }

  override def fetchProductPrice(id: ProductId): F[Option[ProductPrice]] = {
    val q = fr"select * from products.product_price where product_id = $id"

    q.query[ProductPrice].option.transact(transactor)
  }

  override def updateProductPrice(update: ProductPrice): F[Unit] = {
    val q =
      fr"""
          insert into products.product_price (product_id, price, currency)
          values (${update.productId}, ${update.price}, ${update.currency})
          on duplicate key update
            price = ${update.price},
            currency = ${update.currency}
          """

    q.update.run.transact(transactor).void
  }
}
