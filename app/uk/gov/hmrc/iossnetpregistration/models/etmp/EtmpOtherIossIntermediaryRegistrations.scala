/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpOtherIossIntermediaryRegistrations(issuedBy: String, intermediaryNumber: String)

object EtmpOtherIossIntermediaryRegistrations {
  implicit val format: OFormat[EtmpOtherIossIntermediaryRegistrations] = Json.format[EtmpOtherIossIntermediaryRegistrations]
}
