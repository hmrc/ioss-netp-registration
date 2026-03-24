/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}


case class EtmpSchemeDetails(
                              commencementDate: String,
                              euRegistrationDetails: Seq[EtmpEuRegistrationDetails],
                              previousEURegistrationDetails: Seq[EtmpPreviousEuRegistrationDetails],
                              websites: Option[Seq[EtmpWebsite]],
                              contactName: String,
                              businessTelephoneNumber: String,
                              businessEmailId: String,
                              nonCompliantReturns: Option[String],
                              nonCompliantPayments: Option[String]
                            )

object EtmpSchemeDetails {

  implicit val format: OFormat[EtmpSchemeDetails] = Json.format[EtmpSchemeDetails]
}
