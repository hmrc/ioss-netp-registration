/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.requests

import play.api.libs.json.{JsObject, JsValue, Json, OFormat}

case class SaveForLaterRequest(
                                journeyId: String,
                                data: JsObject,
                                intermediaryNumber: String
                              )

object SaveForLaterRequest {

  implicit val format: OFormat[SaveForLaterRequest] = Json.format[SaveForLaterRequest]
}
