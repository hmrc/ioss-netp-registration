/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

import com.mongodb.client.model.{IndexModel, IndexOptions, Indexes, ReplaceOptions}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import play.api.libs.json.Format
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedUserAnswers, SavedUserAnswers}
import uk.gov.hmrc.iossnetpregistration.services.crypto.SavedPendingRegistrationEncryptor
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaveForLaterRepository @Inject()(
                                        mongoComponent: MongoComponent,
                                        appConfig: AppConfig,
                                        savedPendingRegistrationEncryptor: SavedPendingRegistrationEncryptor,
                                      )(implicit executionContext: ExecutionContext)
  extends PlayMongoRepository[EncryptedSavedUserAnswers](
    collectionName = "save-for-later-user-answers",
    mongoComponent = mongoComponent,
    domainFormat = EncryptedSavedUserAnswers.format,
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
          .expireAfter(appConfig.saveForLaterTtl, TimeUnit.DAYS)
      )
    )
  ) with Logging {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byJourneyId(journeyId: String): Bson = {
    Filters.equal("journeyId", journeyId)
  }

  private def byIntermediaryNumber(intermediaryNumber: String): Bson = {
    Filters.equal("intermediaryNumber", intermediaryNumber)
  }

  def set(savedUserAnswers: SavedUserAnswers): Future[SavedUserAnswers] = {

    val encryptedSavedUserAnswers: EncryptedSavedUserAnswers =
      savedPendingRegistrationEncryptor.encryptSaveForLaterAnswers(savedUserAnswers)

    collection
      .replaceOne(
        filter = byJourneyId(savedUserAnswers.journeyId),
        replacement = encryptedSavedUserAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => savedUserAnswers)
  }

  def get(journeyId: String): Future[Option[SavedUserAnswers]] = {
    collection
      .find(
        byJourneyId(journeyId)
      ).headOption()
      .map(_
        .map { encryptedSavedUserAnswers =>
          savedPendingRegistrationEncryptor.decryptSaveForLaterAnswers(encryptedSavedUserAnswers)
        }
      )
  }

  def getSelection(intermediaryNumber: String): Future[Seq[SavedUserAnswers]] = {
  collection
      .find(
        byIntermediaryNumber(intermediaryNumber)
      ).map{ whatIsThis =>
    savedPendingRegistrationEncryptor.decryptSaveForLaterAnswers(whatIsThis)
  }.toFuture()
  }

  def count(intermediaryNumber: String): Future[Long] = {
    collection
      .countDocuments(
        byIntermediaryNumber(intermediaryNumber)
      ).toFuture()
  }

  def clear(journeyId: String): Future[Boolean] = {
    collection
      .deleteOne(
        byJourneyId(journeyId)
      ).toFuture()
      .map(_ => true)
  }
}
