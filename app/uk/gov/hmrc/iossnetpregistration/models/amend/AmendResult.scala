/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.amend

sealed trait AmendResult

object AmendResult {
  case object AmendSucceeded extends AmendResult
  case object AmendFailed extends AmendResult
}
