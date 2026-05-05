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

package uk.gov.hmrc.iossnetpregistration.services

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.iossnetpregistration.models.{PendingRegistrationRequest, SavedPendingRegistration}
import uk.gov.hmrc.iossnetpregistration.repositories.PendingRegistrationRepository

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SavePendingRegistrationService @Inject()(
                                                pendingRegistrationRepository: PendingRegistrationRepository,
                                                uniqueCodeGeneratorService: UniqueCodeGeneratorService
                                              )(implicit ec: ExecutionContext) {

  def savePendingRegistration(pendingRegistrationRequest: PendingRegistrationRequest): Future[SavedPendingRegistration] = {

    val pendingRegistration: SavedPendingRegistration = SavedPendingRegistration(
      journeyId = pendingRegistrationRequest.userAnswers.journeyId,
      uniqueUrlCode = uniqueCodeGeneratorService.generateUniqueCode(),
      userAnswers = pendingRegistrationRequest.userAnswers,
      lastUpdated = pendingRegistrationRequest.userAnswers.lastUpdated,
      uniqueActivationCode = uniqueCodeGeneratorService.generateUniqueCode(),
      intermediaryDetails = pendingRegistrationRequest.intermediaryDetails
    )

    pendingRegistrationRepository.set(pendingRegistration)
  }

  def getPendingRegistration(journeyIdOrUrlCode: String): Future[Option[SavedPendingRegistration]] = {
    pendingRegistrationRepository.get(journeyIdOrUrlCode)
  }

  def getPendingRegistrationsByIntermediaryNumber(intermediaryNumber: String): Future[Seq[SavedPendingRegistration]] = {
    pendingRegistrationRepository.getByIntermediaryNumber(intermediaryNumber)
  }

  def getPendingRegistrationsByCustomerIdentification(
                                                       idType: String,
                                                       idValue: String
                                                     ): Future[Seq[SavedPendingRegistration]] =
    pendingRegistrationRepository.getAll().map { regs =>
      regs.filter { reg =>
        idType match {
          case "VRN" =>
            (reg.userAnswers.data \ "clientVatNumber").asOpt[String].contains(idValue)
          case "NINO" =>
            (reg.userAnswers.data \ "clientsNinoNumber").asOpt[String].contains(idValue)

          case "UTR" =>
            (reg.userAnswers.data \ "clientUtrNumber").asOpt[String].contains(idValue)

          case "FTR" =>
            (reg.userAnswers.data \ "clientTaxRefrence").asOpt[String].contains(idValue)
          case _ =>
            false
        }
      }
    }

  def deletePendingRegistration(journeyId: String): Future[Boolean] = {
    pendingRegistrationRepository.delete(journeyId)
  }
  
  def deleteAllPendingRegistrations(): Future[Boolean] = {
    pendingRegistrationRepository.deleteAll()
  }
  
  def validateClientActivationCode(uniqueUrlCode: String, uniqueActivationCode: String): Future[Option[Boolean]] = {

    val pendingReg: Future[Option[SavedPendingRegistration]] = pendingRegistrationRepository.getDecrypted(uniqueUrlCode)

    pendingReg.map {
      case Some(value) =>
        val isAMatch = value.uniqueActivationCode.toUpperCase == uniqueActivationCode.toUpperCase
        Some(isAMatch)
      case None =>
        None
    }
  }

  def getCount(intermediaryNumber: String): Future[Long] = {
    pendingRegistrationRepository.count(intermediaryNumber)
  }
  
  def updateClientEmailAddress(journeyId: String, newEmailAddress: String): Future[Option[SavedPendingRegistration]] = {
    pendingRegistrationRepository.getDecrypted(journeyId).flatMap {

      case Some(pendingRegistration) =>

        val updatedBusinessContact: JsObject =
          (pendingRegistration.userAnswers.data \ "businessContactDetails").asOpt[JsObject].getOrElse(Json.obj()) ++
            Json.obj("emailAddress" -> newEmailAddress)

        val updatedData: JsObject =
          pendingRegistration.userAnswers.data ++ Json.obj("businessContactDetails" -> updatedBusinessContact)

        val updatedUserAnswers = pendingRegistration.userAnswers.copy(
          data = updatedData,
          lastUpdated = Instant.now
        )

        val updatedPendingRegistration = pendingRegistration.copy(
          userAnswers = updatedUserAnswers
        )

        pendingRegistrationRepository.updateClientEmail(updatedPendingRegistration).map(Some(_))

      case None =>
        Future.successful(None)
    }
  }

}