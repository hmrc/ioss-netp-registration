/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.enrolments

import play.api.libs.json.*

case class SubscriberRequest(serviceName: String, callback: String, etmpId: String)

object SubscriberRequest {
  implicit val format: OFormat[SubscriberRequest] = Json.format[SubscriberRequest]

}