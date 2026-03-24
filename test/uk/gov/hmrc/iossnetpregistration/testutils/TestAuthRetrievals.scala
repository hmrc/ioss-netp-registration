/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.testutils

import uk.gov.hmrc.auth.core.retrieve.~

object TestAuthRetrievals {
  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }
}
