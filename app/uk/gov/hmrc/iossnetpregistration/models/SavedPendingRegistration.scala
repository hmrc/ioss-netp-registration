/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.*

import java.time.Instant

case class SavedPendingRegistration(
                                     journeyId: String,
                                     uniqueUrlCode: String,
                                     userAnswers: UserAnswers,
                                     lastUpdated: Instant,
                                     uniqueActivationCode: String,
                                     intermediaryDetails: IntermediaryDetails
)

object SavedPendingRegistration {

  implicit lazy val format: OFormat[SavedPendingRegistration] = Json.format[SavedPendingRegistration]

}
