/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.display

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpIdType

case class EtmpDisplayCustomerIdentification(idType: EtmpIdType, idValue: String)

object EtmpDisplayCustomerIdentification {

  implicit val format: OFormat[EtmpDisplayCustomerIdentification] = Json.format[EtmpDisplayCustomerIdentification]
}
