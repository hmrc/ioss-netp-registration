/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossnetpregistration.models.{Bic, Iban}

case class EtmpBankDetails(accountName: String, bic: Option[Bic], iban: Iban)

object EtmpBankDetails {

  implicit val format: OFormat[EtmpBankDetails] = Json.format[EtmpBankDetails]
}
