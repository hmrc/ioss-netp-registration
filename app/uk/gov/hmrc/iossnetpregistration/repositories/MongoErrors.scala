/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

object MongoErrors {
  object Duplicate {
    def unapply(ex: Exception): Option[Exception] =
      if (ex.getMessage.contains("E11000")) Some(ex) else None
  }
}