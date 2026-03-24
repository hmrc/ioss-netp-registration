/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpCustomerIdentification(idType: EtmpIdType, idValue: String, iossIntermediaryID: String)

object EtmpCustomerIdentification {

  implicit val format: OFormat[EtmpCustomerIdentification] = Json.format[EtmpCustomerIdentification]
}
