/*
 * Copyright 2026 HM Revenue & Customs
 *
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

  def deletePendingRegistration(journeyId: String): Future[Boolean] = {
    pendingRegistrationRepository.delete(journeyId)
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