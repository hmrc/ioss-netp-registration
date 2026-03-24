/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.generators.Generators
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedUserAnswers, SavedUserAnswers}
import uk.gov.hmrc.iossnetpregistration.services.crypto.SavedPendingRegistrationEncryptor
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, PlayMongoRepositorySupport}

import java.time.temporal.ChronoUnit
import java.time.{Instant, temporal}
import scala.concurrent.ExecutionContext.Implicits.global

class SaveForLaterRepositorySpec
  extends BaseSpec
    with CleanMongoCollectionSupport
    with PlayMongoRepositorySupport[EncryptedSavedUserAnswers]
    with BeforeAndAfterEach
    with Generators {

  private val mockAppConfig: AppConfig = mock[AppConfig]
  private val mockSavedPendingRegistrationEncryptor: SavedPendingRegistrationEncryptor = mock[SavedPendingRegistrationEncryptor]

  private val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

  override protected val repository: SaveForLaterRepository = {
    new SaveForLaterRepository(
      mongoComponent = mongoComponent,
      appConfig = mockAppConfig,
      savedPendingRegistrationEncryptor = mockSavedPendingRegistrationEncryptor
    )
  }

  private val savedUserAnswers: SavedUserAnswers = SavedUserAnswers(
    journeyId = "test-journey-123",
    data = Json.obj(
      "businessName" -> "Test Business Ltd",
      "registrationDate" -> "2024-01-15"
    ),
    intermediaryNumber = "INT123456",
    lastUpdated = now
  )

  private val encryptedSavedUserAnswers: EncryptedSavedUserAnswers = EncryptedSavedUserAnswers(
    journeyId = "test-journey-123",
    data = "encrypted-data-string",
    intermediaryNumber = "INT123456",
    lastUpdated = now
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    deleteAll().futureValue
    org.mockito.Mockito.reset(mockSavedPendingRegistrationEncryptor)
  }

  "SaveForLaterRepository" - {

    ".set" - {

      "must insert SavedUserAnswers" in {

        val testSavedUserAnswers: SavedUserAnswers = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(lastUpdated = now)

        val testEncryptedSavedUserAnswers: EncryptedSavedUserAnswers = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(
          journeyId = testSavedUserAnswers.journeyId,
          intermediaryNumber = testSavedUserAnswers.intermediaryNumber,
          lastUpdated = now
        )

        when(mockSavedPendingRegistrationEncryptor.encryptSaveForLaterAnswers(eqTo(testSavedUserAnswers))) thenReturn testEncryptedSavedUserAnswers

        val result = repository.set(testSavedUserAnswers).futureValue
        val databaseRecord = findAll().futureValue

        result `mustBe` testSavedUserAnswers
        databaseRecord must contain theSameElementsAs Seq(testEncryptedSavedUserAnswers)
        verify(mockSavedPendingRegistrationEncryptor, times(1)).encryptSaveForLaterAnswers(testSavedUserAnswers)
      }

      "must update existing SavedUserAnswers with same journeyId" in {

        val originalSavedUserAnswers: SavedUserAnswers = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(lastUpdated = now.minusSeconds(3600))

        val updatedSavedUserAnswers: SavedUserAnswers = originalSavedUserAnswers.copy(
          data = Json.obj("updatedField" -> "updatedValue"),
          lastUpdated = now
        )

        val originalEncryptedSavedUserAnswers: EncryptedSavedUserAnswers = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(
          journeyId = originalSavedUserAnswers.journeyId,
          intermediaryNumber = originalSavedUserAnswers.intermediaryNumber,
          lastUpdated = originalSavedUserAnswers.lastUpdated
        )

        val updatedEncryptedSavedUserAnswers: EncryptedSavedUserAnswers = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(
          journeyId = updatedSavedUserAnswers.journeyId,
          intermediaryNumber = updatedSavedUserAnswers.intermediaryNumber,
          lastUpdated = updatedSavedUserAnswers.lastUpdated
        )

        // Set up original record
        when(mockSavedPendingRegistrationEncryptor.encryptSaveForLaterAnswers(eqTo(originalSavedUserAnswers))) thenReturn originalEncryptedSavedUserAnswers
        repository.set(originalSavedUserAnswers).futureValue

        // Set up update
        when(mockSavedPendingRegistrationEncryptor.encryptSaveForLaterAnswers(eqTo(updatedSavedUserAnswers))) thenReturn updatedEncryptedSavedUserAnswers

        val result = repository.set(updatedSavedUserAnswers).futureValue
        val databaseRecord = findAll().futureValue

        result `mustBe` updatedSavedUserAnswers
        databaseRecord must contain theSameElementsAs Seq(updatedEncryptedSavedUserAnswers)
        databaseRecord must have size 1
      }
    }

    ".get" - {

      "must return SavedUserAnswers record when one exists for this journeyId" in {

        when(mockSavedPendingRegistrationEncryptor.decryptSaveForLaterAnswers(any())) thenReturn savedUserAnswers

        val updatedAnswers = encryptedSavedUserAnswers
        insert(updatedAnswers).futureValue

        val result = repository.get(updatedAnswers.journeyId).futureValue

        result.value `mustBe` savedUserAnswers
        verify(mockSavedPendingRegistrationEncryptor, times(1)).decryptSaveForLaterAnswers(updatedAnswers)
      }

      "must return None when no record exists for this journeyId" in {

        val nonExistentJourneyId: String = arbitrary[String].sample.value
        insert(encryptedSavedUserAnswers).futureValue

        val result = repository.get(nonExistentJourneyId).futureValue

        result must not be defined
      }
    }

    ".getSelection" - {

      "must return sequence of SavedUserAnswers for matching intermediary number" in {

        val intermediaryNumber = arbitrary[String].sample.value

        val savedUserAnswers1 = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now
        )

        val savedUserAnswers2 = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now.plusSeconds(3600)
        )

        val encryptedSavedUserAnswers1 = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(
          journeyId = savedUserAnswers1.journeyId,
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now
        )

        val encryptedSavedUserAnswers2 = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(
          journeyId = savedUserAnswers2.journeyId,
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now.plusSeconds(3600)
        )

        when(mockSavedPendingRegistrationEncryptor.decryptSaveForLaterAnswers(eqTo(encryptedSavedUserAnswers1))) thenReturn savedUserAnswers1
        when(mockSavedPendingRegistrationEncryptor.decryptSaveForLaterAnswers(eqTo(encryptedSavedUserAnswers2))) thenReturn savedUserAnswers2

        insert(encryptedSavedUserAnswers1).futureValue
        insert(encryptedSavedUserAnswers2).futureValue

        val result = repository.getSelection(intermediaryNumber).futureValue

        result must contain theSameElementsAs Seq(savedUserAnswers1, savedUserAnswers2)
        verify(mockSavedPendingRegistrationEncryptor).decryptSaveForLaterAnswers(eqTo(encryptedSavedUserAnswers1))
        verify(mockSavedPendingRegistrationEncryptor).decryptSaveForLaterAnswers(eqTo(encryptedSavedUserAnswers2))
      }

      "must return empty sequence when no records match intermediary number" in {

        val nonExistentIntermediaryNumber = "INT-NOT-EXISTS"
        insert(encryptedSavedUserAnswers).futureValue

        val result = repository.getSelection(nonExistentIntermediaryNumber).futureValue

        result `mustBe` Seq.empty
      }
    }

    ".count" - {

      "must return a count of saved user answers" in {

        val testSavedUserAnswers: EncryptedSavedUserAnswers= arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(lastUpdated = now)

        insert(testSavedUserAnswers).futureValue

        val result = repository.count(testSavedUserAnswers.intermediaryNumber).futureValue

        result `mustBe` 1
      }
      "must return a count of 0 when there is no saved user answers" in {
        val testSavedUserAnswers: EncryptedSavedUserAnswers = arbitraryEncryptedSavedUserAnswers.arbitrary.sample.value.copy(lastUpdated = now)

        val result = repository.count(testSavedUserAnswers.intermediaryNumber).futureValue

        result `mustBe` 0
      }
    }

    ".clear" - {

      "must delete SavedUserAnswers record when it exists" in {

        insert(encryptedSavedUserAnswers).futureValue
        val recordsBeforeDelete = findAll().futureValue

        val result = repository.clear(encryptedSavedUserAnswers.journeyId).futureValue
        val recordsAfterDelete = findAll().futureValue

        recordsBeforeDelete must contain theSameElementsAs Seq(encryptedSavedUserAnswers)
        result `mustBe` true
        recordsAfterDelete `mustBe` Seq.empty
      }

      "must return true even when no record exists to delete" in {

        val nonExistentJourneyId: String = arbitrary[String].sample.value

        val result = repository.clear(nonExistentJourneyId).futureValue

        result `mustBe` true
      }
    }
  }
}