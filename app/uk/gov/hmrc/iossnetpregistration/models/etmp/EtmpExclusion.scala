/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

import java.time.LocalDate

case class EtmpExclusion(
                          exclusionReason: EtmpExclusionReason,
                          effectiveDate: LocalDate,
                          decisionDate: LocalDate,
                          quarantine: Boolean
                        )


object EtmpExclusion {

  implicit val format: OFormat[EtmpExclusion] = Json.format[EtmpExclusion]
}

sealed trait EtmpExclusionReason

object EtmpExclusionReason extends Enumerable.Implicits {

  case object Reversal extends WithName("-1") with EtmpExclusionReason

  case object NoLongerSupplies extends WithName("1") with EtmpExclusionReason

  case object CeasedTrade extends WithName("2") with EtmpExclusionReason

  case object NoLongerMeetsConditions extends WithName("3") with EtmpExclusionReason

  case object FailsToComply extends WithName("4") with EtmpExclusionReason

  case object VoluntarilyLeaves extends WithName("5") with EtmpExclusionReason

  case object TransferringMSID extends WithName("6") with EtmpExclusionReason

  val values: Seq[EtmpExclusionReason] = Seq(
    Reversal,
    NoLongerSupplies,
    CeasedTrade,
    NoLongerMeetsConditions,
    FailsToComply,
    VoluntarilyLeaves,
    TransferringMSID
  )

  implicit val enumerable: Enumerable[EtmpExclusionReason] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
