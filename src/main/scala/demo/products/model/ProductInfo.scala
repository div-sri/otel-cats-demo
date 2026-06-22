package demo.products.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class ProductInfo(
  id: ProductId,
  name: String,
  description: Option[String],
  createdAt: Instant,
  isActive: Boolean
)

object ProductInfo {
  given Codec[ProductInfo] = deriveCodec
}
