/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.responses

import play.api.libs.json.{Json, OFormat}

case class EtmpEnrolmentErrorResponse(errorDetail: EtmpErrorDetail)

case class EtmpErrorDetail(timestamp: String, errorCode: Option[String], errorMessage: Option[String])

object EtmpEnrolmentErrorResponse {
  implicit val format: OFormat[EtmpEnrolmentErrorResponse] = Json.format[EtmpEnrolmentErrorResponse]
  val alreadyActiveSubscriptionErrorCode = "007"
}

object EtmpErrorDetail {
  implicit val format: OFormat[EtmpErrorDetail] = Json.format[EtmpErrorDetail]
}
