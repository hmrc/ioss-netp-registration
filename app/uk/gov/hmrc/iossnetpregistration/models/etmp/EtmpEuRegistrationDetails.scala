/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpEuRegistrationDetails(
                                    countryOfRegistration: String,
                                    traderId: TraderId,
                                    tradingName: String,
                                    fixedEstablishmentAddressLine1: String,
                                    fixedEstablishmentAddressLine2: Option[String] = None,
                                    townOrCity: String,
                                    regionOrState: Option[String] = None,
                                    postcode: Option[String] = None
                                  )

object EtmpEuRegistrationDetails {

  implicit val format: OFormat[EtmpEuRegistrationDetails] = Json.format[EtmpEuRegistrationDetails]
}
