package demo.products.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ProductQuantity(productId: ProductId, quantity: Double, unit: String)

object ProductQuantity {
  given Codec[ProductQuantity] = deriveCodec
}
