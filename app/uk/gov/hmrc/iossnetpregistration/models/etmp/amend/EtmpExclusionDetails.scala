/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpExclusionDetails(revertExclusion: Boolean,
                                noLongerSupplyGoods: Boolean,
                                noLongerEligible: Boolean,
                                partyType: String,
                                exclusionRequestDate: Option[LocalDate],
                                identificationValidityDate: Option[LocalDate],
                                intExclusionRequestDate: Option[LocalDate],
                                newMemberState: Option[EtmpNewMemberState],
                                establishedMemberState: Option[EtmpEstablishedMemberState])

object EtmpExclusionDetails {

  implicit val format: OFormat[EtmpExclusionDetails] = Json.format[EtmpExclusionDetails]

}
