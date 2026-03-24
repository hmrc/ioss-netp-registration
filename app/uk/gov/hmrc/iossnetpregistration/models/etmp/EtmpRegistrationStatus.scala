/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

sealed trait EtmpRegistrationStatus

object EtmpRegistrationStatus extends Enumerable.Implicits {
  case object Success extends WithName("Success") with EtmpRegistrationStatus
  case object Pending extends WithName("Pending") with EtmpRegistrationStatus
  case object Error extends WithName("Error") with EtmpRegistrationStatus

  val values: Seq[EtmpRegistrationStatus] = Seq(
    Success,
    Pending,
    Error
  )

  implicit val enumerable: Enumerable[EtmpRegistrationStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
