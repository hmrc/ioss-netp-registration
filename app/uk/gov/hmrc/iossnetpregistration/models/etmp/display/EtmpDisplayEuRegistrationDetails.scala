/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.display

import play.api.libs.json.{Json, OFormat}

case class EtmpDisplayEuRegistrationDetails(
                                             issuedBy: String,
                                             vatNumber: Option[String],
                                             taxIdentificationNumber: Option[String],
                                             fixedEstablishmentTradingName: String,
                                             fixedEstablishmentAddressLine1: String,
                                             fixedEstablishmentAddressLine2: Option[String] = None,
                                             townOrCity: String,
                                             regionOrState: Option[String] = None,
                                             postcode: Option[String] = None
                                           )

object EtmpDisplayEuRegistrationDetails {

  implicit val format: OFormat[EtmpDisplayEuRegistrationDetails] = Json.format[EtmpDisplayEuRegistrationDetails]
}
