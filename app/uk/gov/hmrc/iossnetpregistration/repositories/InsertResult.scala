/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

sealed trait InsertResult

object InsertResult {
  case object InsertSucceeded extends InsertResult
  case object AlreadyExists extends InsertResult
}
