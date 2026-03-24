/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossnetpregistration.models.etmp.*

case class EtmpAmendRegistrationRequest(
                                         administration: EtmpAdministration,
                                         changeLog: EtmpAmendRegistrationChangeLog,
                                         exclusionDetails: Option[EtmpExclusionDetails],
                                         customerIdentification: EtmpAmendCustomerIdentification,
                                         tradingNames: Seq[EtmpTradingName],
                                         otherAddress: Option[EtmpOtherAddress],
                                         schemeDetails: EtmpSchemeDetails
                                       )

object EtmpAmendRegistrationRequest {

  implicit val format: OFormat[EtmpAmendRegistrationRequest] = Json.format[EtmpAmendRegistrationRequest]
}
