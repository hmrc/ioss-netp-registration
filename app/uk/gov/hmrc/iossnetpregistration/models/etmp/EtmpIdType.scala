/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

sealed trait EtmpIdType

object EtmpIdType extends Enumerable.Implicits {

  case object VRN extends WithName("VRN") with EtmpIdType
  case object NINO extends WithName("NINO") with EtmpIdType
  case object UTR extends WithName("UTR") with EtmpIdType
  case object FTR extends WithName("FTR") with EtmpIdType

  val values: Seq[EtmpIdType] = Seq(
    VRN,
    NINO,
    UTR,
    FTR
  )

  implicit val enumerable: Enumerable[EtmpIdType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}