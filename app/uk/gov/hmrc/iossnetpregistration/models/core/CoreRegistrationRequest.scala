/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.core

import play.api.libs.json.{Json, OFormat}

case class CoreRegistrationRequest(source: String, scheme: Option[String], searchId: String, searchIntermediary: Option[String], searchIdIssuedBy: String)

object CoreRegistrationRequest {
  implicit val format: OFormat[CoreRegistrationRequest] = Json.format[CoreRegistrationRequest]
}
