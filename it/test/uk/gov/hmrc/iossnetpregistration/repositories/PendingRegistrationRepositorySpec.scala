/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.repositories

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedPendingRegistration, SavedPendingRegistration}
import uk.gov.hmrc.iossnetpregistration.services.UniqueCodeGeneratorService
import uk.gov.hmrc.iossnetpregistration.services.crypto.SavedPendingRegistrationEncryptor
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, PlayMongoRepositorySupport}

import scala.concurrent.ExecutionContext.Implicits.global

class PendingRegistrationRepositorySpec
  extends BaseSpec
    with CleanMongoCollectionSupport
    with PlayMongoRepositorySupport[EncryptedSavedPendingRegistration]
    with BeforeAndAfterEach {

  private val mockAppConfig: AppConfig = mock[AppConfig]
  private val mockSavedPendingRegistrationEncryptor: SavedPendingRegistrationEncryptor = mock[SavedPendingRegistrationEncryptor]
  private val mockUniqueCodeGeneratorService: UniqueCodeGeneratorService = mock[UniqueCodeGeneratorService]

  override protected val repository: PendingRegistrationRepository = {
    new PendingRegistrationRepository(
      mongoComponent = mongoComponent,
      savedPendingRegistrationEncryptor = mockSavedPendingRegistrationEncryptor,
      appConfig = mockAppConfig,
      uniqueCodeGeneratorService = mockUniqueCodeGeneratorService
    )
  }

  private val savedPendingRegistration: SavedPendingRegistration = arbitrarySavedPendingRegistration.arbitrary.sample.value

  private val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration =
    arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value

  def uniqueUrlBsonSearch(searchParam: String): Bson = Filters.equal("uniqueUrlCode", searchParam)

  override def beforeEach(): Unit = {
    super.beforeEach()
    deleteAll().futureValue
  }

  "PendingRegistrationRepository" - {

    ".set" - {

      "must insert SavedPendingRegistration" in {

        val testSavedPendingRegistration: SavedPendingRegistration =
          arbitrarySavedPendingRegistration.arbitrary.sample.value

        val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value

        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(testSavedPendingRegistration))) thenReturn encryptedSavedPendingRegistration

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(encryptedSavedPendingRegistration))) thenReturn testSavedPendingRegistration

        val result = repository.set(testSavedPendingRegistration).futureValue
        
        val databaseRecord = findAll().futureValue

        result `mustBe` testSavedPendingRegistration
        databaseRecord must contain theSameElementsAs Seq(encryptedSavedPendingRegistration)
      }

      "must insert multiple SavedPendingRegistration for different journeyId's with unique uniqueUrlCodes" in {

        val additionalSavedPendingRegistration: SavedPendingRegistration =
          arbitrarySavedPendingRegistration.arbitrary.sample.value

        val additionalEncryptedSavedPendingRegistration: EncryptedSavedPendingRegistration =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value


        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(savedPendingRegistration))) thenReturn encryptedSavedPendingRegistration
        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(additionalSavedPendingRegistration))) thenReturn additionalEncryptedSavedPendingRegistration

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(encryptedSavedPendingRegistration))) thenReturn savedPendingRegistration
        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(additionalEncryptedSavedPendingRegistration))) thenReturn additionalSavedPendingRegistration

        val insertResult1 = repository.set(savedPendingRegistration).futureValue
        val insertResult2 = repository.set(additionalSavedPendingRegistration).futureValue
        val databaseRecord = findAll().futureValue

        insertResult1 `mustBe` savedPendingRegistration
        insertResult2 `mustBe` additionalSavedPendingRegistration
        databaseRecord must contain theSameElementsAs Seq(encryptedSavedPendingRegistration, additionalEncryptedSavedPendingRegistration)
      }

      "must alter uniqueUrlCode when it is not unique and insert SavedPendingRegistrations" in {

        val pendingRegWithMatchingUniqueUrlCode1 = encryptedSavedPendingRegistration.copy(uniqueUrlCode = "ABCDEF")
        val pendingRegWithMatchingUniqueUrlCode2 = encryptedSavedPendingRegistration.copy(uniqueUrlCode = "ABCDEF")


        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(savedPendingRegistration))) thenReturn pendingRegWithMatchingUniqueUrlCode1
        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(pendingRegWithMatchingUniqueUrlCode1))) thenReturn savedPendingRegistration
        repository.set(savedPendingRegistration).futureValue
        val insertResult1 = find(uniqueUrlBsonSearch(pendingRegWithMatchingUniqueUrlCode1.uniqueUrlCode)).futureValue

        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(savedPendingRegistration))) thenReturn pendingRegWithMatchingUniqueUrlCode2
        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(pendingRegWithMatchingUniqueUrlCode2))) thenReturn savedPendingRegistration
        when(mockUniqueCodeGeneratorService.generateUniqueCode()) thenReturn "GHIJKL"
        repository.set(savedPendingRegistration).futureValue
        val insertResult2 = find(uniqueUrlBsonSearch("GHIJKL")).futureValue

        insertResult1 `mustBe` List(pendingRegWithMatchingUniqueUrlCode1)
        insertResult2 `mustBe` List(pendingRegWithMatchingUniqueUrlCode2.copy(uniqueUrlCode = "GHIJKL"))
        verify(mockUniqueCodeGeneratorService, times(1)).generateUniqueCode()
      }
    }

    ".get" - {

      " must return SavedPendingRegistration record when one exists for this journeyId" in {

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(any())) thenReturn savedPendingRegistration

        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue

        val result = repository.get(updatedAnswers.journeyId).futureValue

        result.value `mustBe` savedPendingRegistration
      }

      " must return None when no record exists for this journeyId" in {

        val nonExistentJourneyId: String = arbitrary[String].sample.value

        insert(encryptedSavedPendingRegistration).futureValue

        val result = repository.get(nonExistentJourneyId).futureValue

        result must not be defined
      }
    }

    ".getByIntermediaryNumber" - {

      "must return SavedPendingRegistration record when one exists for this intermediary" in {

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(any())) thenReturn savedPendingRegistration

        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue

        val result = repository.getByIntermediaryNumber(updatedAnswers.intermediaryDetails.intermediaryNumber).futureValue

        result `mustBe` Seq(savedPendingRegistration)
      }

      "must return multiple SavedPendingRegistration records when more than one exists for this intermediary" in {
        val commonIntermediaryNumber = "IM1234567"
        val savedPendingRegistration1: SavedPendingRegistration =
          arbitrarySavedPendingRegistration.arbitrary.sample.value.copy(
            intermediaryDetails = arbitrarySavedPendingRegistration.arbitrary.sample.value.intermediaryDetails.copy(
              intermediaryNumber = commonIntermediaryNumber
            )
          )

        val savedPendingRegistration2: SavedPendingRegistration =
          arbitrarySavedPendingRegistration.arbitrary.sample.value.copy(
            intermediaryDetails = arbitrarySavedPendingRegistration.arbitrary.sample.value.intermediaryDetails.copy(
              intermediaryNumber = commonIntermediaryNumber
            )
          )

        val encrypted1: EncryptedSavedPendingRegistration =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value.copy(
            intermediaryDetails = savedPendingRegistration1.intermediaryDetails
          )

        val encrypted2: EncryptedSavedPendingRegistration =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value.copy(
            intermediaryDetails = savedPendingRegistration2.intermediaryDetails
          )

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(encrypted1))
          .thenReturn(savedPendingRegistration1)
        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(encrypted2))
          .thenReturn(savedPendingRegistration2)

        insert(encrypted1).futureValue
        insert(encrypted2).futureValue

        val result = repository.getByIntermediaryNumber(commonIntermediaryNumber).futureValue

        result `mustBe` Seq(savedPendingRegistration1, savedPendingRegistration2)
      }

      "must return None when no record exists for this intermediary" in {

        val nonExistentIntermediaryNumber: String = arbitrary[String].sample.value

        insert(encryptedSavedPendingRegistration).futureValue

        val result = repository.getByIntermediaryNumber(nonExistentIntermediaryNumber).futureValue

        result `mustBe` Seq.empty
      }
    }

    ".getAll" - {

      "must return SavedPendingRegistration record" in {

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(any())) thenReturn savedPendingRegistration

        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue

        val result = repository.getAll().futureValue

        result `mustBe` Seq(savedPendingRegistration)
      }

      "must return multiple SavedPendingRegistration records" in {

        val savedPendingRegistration1 =
          arbitrarySavedPendingRegistration.arbitrary.sample.value

        val savedPendingRegistration2 =
          arbitrarySavedPendingRegistration.arbitrary.sample.value

        val encrypted1 =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value.copy(
            journeyId = savedPendingRegistration1.journeyId
          )

        val encrypted2 =
          arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value.copy(
            journeyId = savedPendingRegistration2.journeyId
          )

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(encrypted1))
          .thenReturn(savedPendingRegistration1)

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(encrypted2))
          .thenReturn(savedPendingRegistration2)

        insert(encrypted1).futureValue
        insert(encrypted2).futureValue

        val result = repository.getAll().futureValue

        result must contain theSameElementsAs Seq(
          savedPendingRegistration1,
          savedPendingRegistration2
        )
      }

      "must return an empty sequence when no records exist" in {

        val result = repository.getAll().futureValue

        result `mustBe` Seq.empty
      }
    }

    ".getDecrypted" - {

      " must return SavedPendingRegistration record when one exists for this uniqueUrlCode" in {

        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(any())) thenReturn savedPendingRegistration

        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue

        val result = repository.getDecrypted(updatedAnswers.uniqueUrlCode).futureValue

        result.value `mustBe` savedPendingRegistration
      }

      " must return None when no record exists for this uniqueUrlCode" in {

        val nonExistentJourneyId: String = arbitrary[String].sample.value

        insert(encryptedSavedPendingRegistration).futureValue

        val result = repository.getDecrypted(nonExistentJourneyId).futureValue

        result must not be defined
      }
    }

    ".count" - {

      "must return a count of pending registrations" in {

        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue

        val result = repository.count(encryptedSavedPendingRegistration.intermediaryDetails.intermediaryNumber).futureValue

        result `mustBe` 1
      }
    }

    ".delete" - {

      "must delete a pending registration detail if it exists in the database" in {
        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue
        val result = repository.delete(encryptedSavedPendingRegistration.journeyId).futureValue

        result `mustBe` true
      }
    }

    ".deleteAll" - {

      "must delete a pending registration detail if it exists in the database" in {
        val updatedAnswers = encryptedSavedPendingRegistration

        insert(updatedAnswers).futureValue
        val result = repository.deleteAll().futureValue

        result `mustBe` true
      }
    }

    ".updateClientEmail" - {

      "must return an updated SavedPendingRegistration record" in {

        when(mockSavedPendingRegistrationEncryptor.encryptAnswers(eqTo(savedPendingRegistration))) thenReturn encryptedSavedPendingRegistration
        when(mockSavedPendingRegistrationEncryptor.decryptUserAnswers(eqTo(encryptedSavedPendingRegistration))) thenReturn savedPendingRegistration

        val result = repository.updateClientEmail(savedPendingRegistration).futureValue

        result `mustBe` savedPendingRegistration
      }
    }
  }
}