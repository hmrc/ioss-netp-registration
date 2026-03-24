/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class AmendRegistrationResponse(
                                      processingDateTime: LocalDateTime,
                                      formBundleNumber: String,
                                      iossReference: String,
                                      businessPartner: String
                                    )

object AmendRegistrationResponse {

  implicit val format: OFormat[AmendRegistrationResponse] = Json.format[AmendRegistrationResponse]

}
