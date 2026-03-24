/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{Json, OFormat}

case class PendingRegistrationRequest(
                                       userAnswers: UserAnswers,
                                       intermediaryDetails: IntermediaryDetails
                                     )

object PendingRegistrationRequest {
  implicit lazy val format: OFormat[PendingRegistrationRequest] = Json.format[PendingRegistrationRequest]
}

case class IntermediaryDetails(intermediaryNumber: String, intermediaryName: String)

object IntermediaryDetails {
  implicit lazy val format: OFormat[IntermediaryDetails] = Json.format[IntermediaryDetails]
}