/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

sealed trait SchemeType

object SchemeType extends Enumerable.Implicits {

  case object OSSUnion extends WithName("OSS Union") with SchemeType
  case object OSSNonUnion extends WithName("OSS Non-Union") with SchemeType
  case object IOSSWithoutIntermediary extends WithName("IOSS without intermediary") with SchemeType
  case object IOSSWithIntermediary extends WithName("IOSS with intermediary") with SchemeType

  val values: Seq[SchemeType] = Seq(
    OSSUnion, OSSNonUnion, IOSSWithoutIntermediary, IOSSWithIntermediary
  )

  implicit val enumerable: Enumerable[SchemeType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
