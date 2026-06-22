package demo.products.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ProductPrice(productId: ProductId, price: Double, currency: String)

object ProductPrice {
  given Codec[ProductPrice] = deriveCodec
}
