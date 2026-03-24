/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services.crypto

import org.scalacheck.Arbitrary.arbitrary
import play.api.Configuration
import play.api.test.Helpers.running
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class EncryptionServiceSpec extends BaseSpec {

  private val encryptionText: String = arbitrary[String].sample.value

  "EncryptionService" - {

    "must encrypt text field when invoked" in {

      val application = applicationBuilder().build()

      running(application) {

        val configuration: Configuration = application.configuration

        val service: EncryptionService = new EncryptionService(configuration)

        val result = service.encryptField(encryptionText)

        result `mustBe` a[String]
        result must not be encryptionText
      }
    }

    "must decrypt encrypted field when invoked" in {

      val application = applicationBuilder().build()

      running(application) {

        val configuration: Configuration = application.configuration

        val service: EncryptionService = new EncryptionService(configuration)

        val encryptedText: String = service.encryptField(encryptionText)

        val result = service.decryptField(encryptedText)

        result `mustBe` a[String]
        result `mustBe` encryptionText
      }
    }

    "must throw a Security Exception if encrypted text value can't be decrypted" in {

      val application = applicationBuilder().build()

      running(application) {

        val configuration: Configuration = application.configuration

        val service: EncryptionService = new EncryptionService(configuration)

        val invalidEncryptedValue = service.encryptField(encryptionText) + arbitrary[String].sample.value

        val result = intercept[SecurityException](service.decryptField(invalidEncryptedValue))
        result.getMessage mustBe "Unable to decrypt value"
      }
    }
  }
}
