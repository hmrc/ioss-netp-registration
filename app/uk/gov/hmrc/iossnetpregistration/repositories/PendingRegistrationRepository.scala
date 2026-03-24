/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

import jakarta.inject.Singleton
import org.mongodb.scala.bson.conversions.*
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import play.api.libs.json.Format
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedPendingRegistration, SavedPendingRegistration}
import uk.gov.hmrc.iossnetpregistration.services.UniqueCodeGeneratorService
import uk.gov.hmrc.iossnetpregistration.services.crypto.SavedPendingRegistrationEncryptor
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PendingRegistrationRepository @Inject()(
                                               val mongoComponent: MongoComponent,
                                               savedPendingRegistrationEncryptor: SavedPendingRegistrationEncryptor,
                                               appConfig: AppConfig,
                                               uniqueCodeGeneratorService: UniqueCodeGeneratorService
                                             )(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[EncryptedSavedPendingRegistration](
    collectionName = "pending-registration",
    mongoComponent = mongoComponent,
    domainFormat = EncryptedSavedPendingRegistration.format,
    replaceIndexes = true,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("journeyId"),
        IndexOptions()
          .name("journeyIdIdx")
          .unique(true)
      ),
      IndexModel(
        Indexes.ascending("lastUpdated"),
        IndexOptions()
          .name("lastUpdatedIdx")
          .expireAfter(appConfig.pendingRegistrationStatusTtl, TimeUnit.DAYS)
      )
    )
  ) with Logging {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byJourneyIdOrUrlCode(journeyIdOrUniqueCode: String): Bson = {
    val filter = {
      if journeyIdOrUniqueCode.length == 6 then
        "uniqueUrlCode"
      else
        "journeyId"
    }
    Filters.equal(filter, journeyIdOrUniqueCode)
  }

  private def byIntermediaryNumber(intermediaryNumber: String): Bson = {
    Filters.equal("intermediaryDetails.intermediaryNumber", intermediaryNumber)
  }

  private def ensureUnique(pendingReg: EncryptedSavedPendingRegistration): Future[EncryptedSavedPendingRegistration] = {
    val existing = collection.find(equal("uniqueUrlCode", pendingReg.uniqueUrlCode))
      .first()
      .head()
      .map { value =>
        !value.isInstanceOf[EncryptedSavedPendingRegistration]
      }

    existing.flatMap {
      case true =>
        Future.successful(pendingReg)
      case false =>
        val updatedReg = pendingReg.copy(uniqueUrlCode = uniqueCodeGeneratorService.generateUniqueCode())
        ensureUnique(updatedReg)
    }
  }

  def set(pendingRegistration: SavedPendingRegistration): Future[SavedPendingRegistration] = {

    val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration =
      savedPendingRegistrationEncryptor.encryptAnswers(pendingRegistration)


    val uniqueEncryptedSavedPendingRegistration: Future[EncryptedSavedPendingRegistration] = ensureUnique(encryptedSavedPendingRegistration)

    uniqueEncryptedSavedPendingRegistration.flatMap { pendingReg =>
      collection
        .replaceOne(
          filter = byJourneyIdOrUrlCode(pendingReg.journeyId),
          replacement = pendingReg,
          options = ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map(_ => savedPendingRegistrationEncryptor.decryptUserAnswers(pendingReg))
    }
  }
  
  def get(journeyIdOrUrlCode: String): Future[Option[SavedPendingRegistration]] = {
    collection
      .find(
        byJourneyIdOrUrlCode(journeyIdOrUrlCode)
      ).headOption()
      .map(_.map(savedPendingRegistrationEncryptor.decryptUserAnswers))
  }

  def getByIntermediaryNumber(intermediaryNumber: String): Future[Seq[SavedPendingRegistration]] = {
    collection
      .find(
        byIntermediaryNumber(intermediaryNumber)
      ).toFuture()
      .map(_.map(savedPendingRegistrationEncryptor.decryptUserAnswers))
  }

  def getDecrypted(uniqueUrlCode: String): Future[Option[SavedPendingRegistration]] = {
    collection
      .find(
        byJourneyIdOrUrlCode(uniqueUrlCode)
      ).headOption()
      .map(_.map(savedPendingRegistrationEncryptor.decryptUserAnswers))
  }

  def count(intermediaryNumber: String): Future[Long] = {
    collection
      .countDocuments(
        byIntermediaryNumber(intermediaryNumber)
      ).toFuture()
  }
  
  def delete(journeyId: String): Future[Boolean] = {
    collection
      .deleteOne(byJourneyIdOrUrlCode(journeyId))
      .toFuture()
      .map(_ => true)
  }

  def updateClientEmail(pendingRegistration: SavedPendingRegistration): Future[SavedPendingRegistration] = {

    val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration =
      savedPendingRegistrationEncryptor.encryptAnswers(pendingRegistration)
    
    collection
      .replaceOne(
        filter = byJourneyIdOrUrlCode(encryptedSavedPendingRegistration.journeyId),
        replacement = encryptedSavedPendingRegistration,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => savedPendingRegistrationEncryptor.decryptUserAnswers(encryptedSavedPendingRegistration))
  }
}
