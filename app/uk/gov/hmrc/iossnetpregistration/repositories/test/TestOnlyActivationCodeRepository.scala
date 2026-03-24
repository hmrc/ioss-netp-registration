/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories.test

import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.libs.json.Format
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedPendingRegistration, SavedPendingRegistration}
import uk.gov.hmrc.iossnetpregistration.services.crypto.SavedPendingRegistrationEncryptor
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOnlyActivationCodeRepository @Inject()(
                                                   val mongoComponent: MongoComponent,
                                                   savedPendingRegistrationEncryptor: SavedPendingRegistrationEncryptor,
                                                   appConfig: AppConfig
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
          .expireAfter(appConfig.pendingRegistrationStatusTtl, TimeUnit.DAYS)
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

  private def byUrlCode(uniqueUrlCode: String): Bson = Filters.equal("uniqueUrlCode", uniqueUrlCode)

  def getDecryptedAnswer(uniqueUrlCode: String): Future[Option[SavedPendingRegistration]] = {
    collection
      .find(
        byUrlCode(uniqueUrlCode)
      ).headOption()
      .map(_.map(savedPendingRegistrationEncryptor.decryptUserAnswers))
  }
}

