/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.audit

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}


sealed trait SubmissionResult

object SubmissionResult extends Enumerable.Implicits {

  case object Success extends WithName("success") with SubmissionResult

  case object Failure extends WithName("failure") with SubmissionResult

  case object Duplicate extends WithName("enrolment-already-existed") with SubmissionResult

  val values: Seq[SubmissionResult] = Seq(Success, Failure, Duplicate)

  implicit val enumerable: Enumerable[SubmissionResult] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
