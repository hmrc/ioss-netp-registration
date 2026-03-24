/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.responses

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class EtmpEnrolmentResponse(
                                  processingDateTime: LocalDateTime,
                                  formBundleNumber: String,
                                  iossReference: String,
                                  businessPartner: String
                                )

object EtmpEnrolmentResponse {

  implicit val format: OFormat[EtmpEnrolmentResponse] = Json.format[EtmpEnrolmentResponse]
}
