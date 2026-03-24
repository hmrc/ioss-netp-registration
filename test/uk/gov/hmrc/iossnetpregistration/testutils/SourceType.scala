package uk.gov.hmrc.iossnetpregistration.testutils

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}


sealed trait SourceType

object SourceType extends Enumerable.Implicits {
  case object VATNumber extends WithName("VATNumber") with SourceType

  case object EUTraderId extends WithName("EUTraderId") with SourceType

  case object TraderId extends WithName("TraderId") with SourceType

  val values: Seq[SourceType] = Seq(
    VATNumber,
    EUTraderId,
    TraderId
  )

  implicit val enumerable: Enumerable[SourceType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
