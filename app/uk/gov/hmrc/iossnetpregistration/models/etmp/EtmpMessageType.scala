/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

sealed trait EtmpMessageType

object EtmpMessageType extends Enumerable.Implicits {

  case object IOSSIntAddClient extends WithName("IOSSIntAddClient") with EtmpMessageType
  case object IOSSIntAmendClient extends WithName("IOSSIntAmendClient") with EtmpMessageType

  val values: Seq[EtmpMessageType] = Seq(
    IOSSIntAddClient, IOSSIntAmendClient
  )

  implicit val enumerable: Enumerable[EtmpMessageType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}