/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpRegistrationRequest(
                                    administration: EtmpAdministration,
                                    customerIdentification: EtmpCustomerIdentification,
                                    tradingNames: Seq[EtmpTradingName],
                                    intermediaryDetails: Option[EtmpIntermediaryDetails],
                                    otherAddress: Option[EtmpOtherAddress],
                                    schemeDetails: EtmpSchemeDetails,
                                    bankDetails: Option[EtmpBankDetails]
                                  )

object EtmpRegistrationRequest {

  implicit val format: OFormat[EtmpRegistrationRequest] = Json.format[EtmpRegistrationRequest]
}