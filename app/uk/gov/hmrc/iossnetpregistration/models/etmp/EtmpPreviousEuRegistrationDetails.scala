/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpPreviousEuRegistrationDetails(
                                            issuedBy: String,
                                            registrationNumber: String,
                                            schemeType: SchemeType,
                                            intermediaryNumber: Option[String] = None
                                          )

object EtmpPreviousEuRegistrationDetails {

  implicit val format: OFormat[EtmpPreviousEuRegistrationDetails] = Json.format[EtmpPreviousEuRegistrationDetails]
}
