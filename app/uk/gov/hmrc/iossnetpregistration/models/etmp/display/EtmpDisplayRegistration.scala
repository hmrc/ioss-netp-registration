/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.display

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Json, Reads, Writes, __}
import uk.gov.hmrc.iossnetpregistration.models.etmp.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.EtmpDisplaySchemeDetails.displaySchemeDetailsReads

case class EtmpDisplayRegistration(
                                    customerIdentification: EtmpDisplayCustomerIdentification,
                                    tradingNames: Seq[EtmpTradingName],
                                    otherAddress: Option[EtmpOtherAddress],
                                    schemeDetails: EtmpDisplaySchemeDetails,
                                    exclusions: Seq[EtmpExclusion],
                                    adminUse: EtmpAdminUse
                                  )

object EtmpDisplayRegistration {

  implicit private val etmpDisplaySchemeDetailsReads: Reads[EtmpDisplaySchemeDetails] = displaySchemeDetailsReads

  implicit val reads: Reads[EtmpDisplayRegistration] =
    (
      (__ \ "customerIdentification").read[EtmpDisplayCustomerIdentification] and
        (__ \ "tradingNames").readNullable[Seq[EtmpTradingName]].map(_.getOrElse(List.empty)) and
        (__ \ "otherAddress").readNullable[EtmpOtherAddress] and
        (__ \ "schemeDetails").read[EtmpDisplaySchemeDetails] and
        (__ \ "exclusions").readNullable[Seq[EtmpExclusion]].map(_.getOrElse(List.empty)) and
        (__ \ "adminUse").read[EtmpAdminUse]
      )(EtmpDisplayRegistration.apply _)

  implicit val writes: Writes[EtmpDisplayRegistration] = Json.writes[EtmpDisplayRegistration]
}