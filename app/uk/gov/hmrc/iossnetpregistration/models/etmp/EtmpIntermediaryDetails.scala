/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpIntermediaryDetails(otherIossIntermediaryRegistrations: Seq[EtmpOtherIossIntermediaryRegistrations])

object EtmpIntermediaryDetails {
  implicit val format: OFormat[EtmpIntermediaryDetails] = Json.format[EtmpIntermediaryDetails]
}


