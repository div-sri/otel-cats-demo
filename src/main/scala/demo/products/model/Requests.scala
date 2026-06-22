package demo.products.model

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CreateNewProduct(
  name: String,
  description: Option[String]
)

object CreateNewProduct {
  given Codec[CreateNewProduct] = deriveCodec
}

case class UpdateProductQuantityRequest(
  quantity: Double,
  unit: String
)

object UpdateProductQuantityRequest {
  given Codec[UpdateProductQuantityRequest] = deriveCodec
}

case class UpdateProductPriceRequest(
  price: Double,
  currency: String
)

object UpdateProductPriceRequest {
  given Codec[UpdateProductPriceRequest] = deriveCodec
}
