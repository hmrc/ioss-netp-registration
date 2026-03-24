/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services.test

import uk.gov.hmrc.iossnetpregistration.models.SavedPendingRegistration
import uk.gov.hmrc.iossnetpregistration.repositories.test.TestOnlyActivationCodeRepository

import javax.inject.Inject
import scala.concurrent.Future

class TestOnlyActivationCodeService @Inject()(
                                               activationCodeRepository: TestOnlyActivationCodeRepository
                                              ) {
  def getPendingRegistration(uniqueUrlCode: String): Future[Option[SavedPendingRegistration]] = {
    activationCodeRepository.getDecryptedAnswer(uniqueUrlCode)
  }
}
