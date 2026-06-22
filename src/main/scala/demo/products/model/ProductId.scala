package demo.products.model

import cats.effect.Sync
import cats.effect.std.UUIDGen
import cats.syntax.either.*
import cats.syntax.functor.*
import doobie.{Get, Put}
import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

import java.util.UUID

case class ProductId(id: UUID) extends AnyVal

object ProductId {

  // circe codec
  given Encoder[ProductId] = Encoder[String].contramap(_.id.toString)
  given Decoder[ProductId] = Decoder[String].emap(fromString)

  // doobie mapping
  given Get[ProductId] = Get[String].temap(fromString)
  given Put[ProductId] = Put[String].contramap(_.id.toString)

  // swagger schema mapping
  given Schema[ProductId] = Schema.schemaForUUID.as[ProductId]

  def fromString(id: String): Either[String, ProductId] =
    Either.catchNonFatal(UUID.fromString(id)).leftMap(_.getMessage).map(ProductId(_))

  def gen[F[_] : {UUIDGen, Sync}]: F[ProductId] = UUIDGen.randomUUID[F].map(ProductId(_))
}
