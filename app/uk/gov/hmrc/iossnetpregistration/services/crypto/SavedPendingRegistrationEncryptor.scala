/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.iossnetpregistration.services.crypto

import play.api.libs.json.*
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.*

import javax.inject.Inject

class SavedPendingRegistrationEncryptor @Inject()(
                                                   encryptionService: EncryptionService
                                                 ) extends Logging {


  def encryptAnswers(savedPendingRegistration: SavedPendingRegistration): EncryptedSavedPendingRegistration = {

    def encryptAnswerValue(answerValue: String): String = encryptionService.encryptField(answerValue)

    EncryptedSavedPendingRegistration(
      journeyId = savedPendingRegistration.journeyId,
      uniqueUrlCode = savedPendingRegistration.uniqueUrlCode,
      data = encryptAnswerValue(Json.toJson(savedPendingRegistration.userAnswers).toString),
      lastUpdated = savedPendingRegistration.lastUpdated,
      uniqueActivationCode = encryptAnswerValue(savedPendingRegistration.uniqueActivationCode),
      intermediaryDetails = savedPendingRegistration.intermediaryDetails
    )
  }

  def encryptSaveForLaterAnswers(savedUserAnswers: SavedUserAnswers): EncryptedSavedUserAnswers = {

    def encryptAnswerValue(answerValue: String): String = encryptionService.encryptField(answerValue)

    EncryptedSavedUserAnswers(
      journeyId = savedUserAnswers.journeyId,
      data = encryptAnswerValue(Json.toJson(savedUserAnswers.data).toString),
      intermediaryNumber = savedUserAnswers.intermediaryNumber,
      lastUpdated = savedUserAnswers.lastUpdated
    )
  }

  def decryptUserAnswers(encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration): SavedPendingRegistration = {

    def decryptAnswerValue(encryptedValue: String): String = encryptionService.decryptField(encryptedValue)

    val jsonParseResult = Json.parse(decryptAnswerValue(encryptedSavedPendingRegistration.data)).validate[UserAnswers] match {
      case JsSuccess(value, _) => value
      case JsError(errors) =>
        val message: String = s"Failed trying to parse UserAnswers JSON with errors: $errors"
        val exception = new Exception(message)
        logger.error(exception.getMessage, exception)
        throw exception
    }

    SavedPendingRegistration(
      journeyId = encryptedSavedPendingRegistration.journeyId,
      uniqueUrlCode = encryptedSavedPendingRegistration.uniqueUrlCode,
      userAnswers = jsonParseResult,
      lastUpdated = encryptedSavedPendingRegistration.lastUpdated,
      uniqueActivationCode = decryptAnswerValue(encryptedSavedPendingRegistration.uniqueActivationCode),
      intermediaryDetails = encryptedSavedPendingRegistration.intermediaryDetails
    )
  }

  def decryptSaveForLaterAnswers(encryptedSavedUserAnswers: EncryptedSavedUserAnswers): SavedUserAnswers = {

    def decryptAnswerValue(encryptedValue: String): String = encryptionService.decryptField(encryptedValue)
    
    SavedUserAnswers(
      journeyId = encryptedSavedUserAnswers.journeyId,
      data = Json.parse(decryptAnswerValue(encryptedSavedUserAnswers.data)).as[JsObject],
      intermediaryNumber = encryptedSavedUserAnswers.intermediaryNumber,
      lastUpdated = encryptedSavedUserAnswers.lastUpdated
    )
  }
}
