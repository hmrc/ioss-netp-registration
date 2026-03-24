/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpOtherAddress(
                             issuedBy: String,
                             tradingName: Option[String],
                             addressLine1: String,
                             addressLine2: Option[String],
                             townOrCity: String,
                             regionOrState: Option[String],
                             postcode: Option[String]
                           )


object EtmpOtherAddress {
  implicit val format: OFormat[EtmpOtherAddress] = Json.format[EtmpOtherAddress]
}
