/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services.crypto

import jakarta.inject.Singleton
import play.api.Configuration
import uk.gov.hmrc.crypto.*

import javax.inject.Inject

@Singleton
class EncryptionService @Inject(configuration: Configuration) {

  protected lazy val crypto: Encrypter & Decrypter = SymmetricCryptoFactory.aesGcmCryptoFromConfig(
    baseConfigKey = "mongodb.encryption",
    config = configuration.underlying
  )

  def encryptField(rawValue: String): String = {
    crypto.encrypt(PlainText(rawValue)).value
  }

  def decryptField(encryptedValue: String): String = {
    crypto.decrypt(Crypted(encryptedValue)).value
  }
}
