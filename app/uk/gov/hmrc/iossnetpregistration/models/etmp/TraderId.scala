/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat, Reads, Writes}

sealed trait TraderId

object TraderId {

  implicit val reads: Reads[TraderId] =
    VatNumberTraderId.format.widen[TraderId] orElse
      TaxRefTraderID.format.widen[TraderId]

  implicit val writes: Writes[TraderId] = Writes {
    case v: VatNumberTraderId => Json.toJson(v)(VatNumberTraderId.format)
    case tr: TaxRefTraderID => Json.toJson(tr)(TaxRefTraderID.format)
  }
}

final case class VatNumberTraderId(vatNumber: String) extends TraderId

object VatNumberTraderId {

  implicit val format: OFormat[VatNumberTraderId] = Json.format[VatNumberTraderId]
}

final case class TaxRefTraderID(taxReferenceNumber: String) extends TraderId

object TaxRefTraderID {

  implicit val format: OFormat[TaxRefTraderID] = Json.format[TaxRefTraderID]
}