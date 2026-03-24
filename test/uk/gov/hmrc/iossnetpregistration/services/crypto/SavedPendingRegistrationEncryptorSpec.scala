package uk.gov.hmrc.iossnetpregistration.services.crypto

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.{EncryptedSavedPendingRegistration, EncryptedSavedUserAnswers, SavedPendingRegistration, SavedUserAnswers}

import java.time.Instant

class SavedPendingRegistrationEncryptorSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockEncryptionService: EncryptionService = mock[EncryptionService]

  private val savedPendingRegistration: SavedPendingRegistration = arbitrarySavedPendingRegistration.arbitrary.sample.value

  private val savedUserAnswers: SavedUserAnswers = SavedUserAnswers(
    journeyId = "test-journey-id",
    data = Json.obj(
      "field1" -> "value1",
      "field2" -> "value2",
      "nested" -> Json.obj("subfield" -> "subvalue")
    ),
    intermediaryNumber = "IN123456",
    lastUpdated = Instant.now()
  )

  override def beforeEach(): Unit = {
    Mockito.reset(mockEncryptionService)
  }

  "SavedPendingRegistrationEncryptor" - {

    ".encryptAnswers" - {

      "must encrypt a SavedPendingRegistration and return an EncryptedSavedPendingRegistration" in {

        val encryptor = new SavedPendingRegistrationEncryptor(mockEncryptionService)

        val result = encryptor.encryptAnswers(savedPendingRegistration)

        val expectedAnswers: EncryptedSavedPendingRegistration = {
          EncryptedSavedPendingRegistration(
            journeyId = savedPendingRegistration.journeyId,
            uniqueUrlCode = savedPendingRegistration.uniqueUrlCode,
            data = result.data,
            lastUpdated = savedPendingRegistration.lastUpdated,
            uniqueActivationCode = result.uniqueActivationCode,
            intermediaryDetails = savedPendingRegistration.intermediaryDetails
          )
        }

        val userAnswersAsString = Json.toJson(savedPendingRegistration.userAnswers).toString

        result `mustBe` expectedAnswers
        result.uniqueActivationCode must not be savedPendingRegistration.uniqueActivationCode
        result.data must not be userAnswersAsString
        verify(mockEncryptionService, times(2)).encryptField(any())
      }
    }

    ".encryptSaveForLaterAnswers" - {
      "must encrypt SaveForLaterAnswers and return EncryptedSavedUserAnswers" in {

        when(mockEncryptionService.encryptField(any())).thenReturn("encrypted-data")

        val encryptor = new SavedPendingRegistrationEncryptor(mockEncryptionService)

        val result = encryptor.encryptSaveForLaterAnswers(savedUserAnswers)

        val expectedAnswers: EncryptedSavedUserAnswers = {
          EncryptedSavedUserAnswers(
            journeyId = savedUserAnswers.journeyId,
            data = "encrypted-data",
            intermediaryNumber = savedUserAnswers.intermediaryNumber,
            lastUpdated = savedUserAnswers.lastUpdated
          )
        }

        val userDataAsString = Json.toJson(savedUserAnswers.data).toString

        result `mustBe` expectedAnswers
        result.data must not be userDataAsString

        verify(mockEncryptionService).encryptField(userDataAsString)
        verify(mockEncryptionService, times(1)).encryptField(any())
      }
    }

    ".decryptUserAnswers" - {

      "must decrypt an EncryptedSavedPendingRegistration and return a SavedPendingRegistration" in {

        val encryptedUserAnswers: String = "EncryptedUserAnswers"
        val encryptedUniqueActivationCode: String = "EncryptedUniqueActivationCode"

        val encryptor = new SavedPendingRegistrationEncryptor(mockEncryptionService)

        when(mockEncryptionService.encryptField(eqTo(Json.toJson(savedPendingRegistration.userAnswers).toString))) thenReturn encryptedUserAnswers
        when(mockEncryptionService.encryptField(eqTo(savedPendingRegistration.uniqueActivationCode))) thenReturn encryptedUniqueActivationCode

        val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration = encryptor.encryptAnswers(savedPendingRegistration)

        when(mockEncryptionService.decryptField(eqTo(encryptedUserAnswers))) thenReturn Json.toJson(savedPendingRegistration.userAnswers).toString
        when(mockEncryptionService.decryptField(eqTo(encryptedUniqueActivationCode))) thenReturn Json.toJson(savedPendingRegistration.uniqueActivationCode).toString

        val result = encryptor.decryptUserAnswers(encryptedSavedPendingRegistration)

        withClue("Should decrypt userAnswers") {
          result.userAnswers mustEqual savedPendingRegistration.userAnswers
        }

        result.getClass `mustBe` savedPendingRegistration.getClass
        verify(mockEncryptionService, times(2)).decryptField(any())
      }

      "must throw an Exception when decrypted JSON can't be parsed to a UserAnswers object" in {

        val encryptedUniqueActivationCode: String = "EncryptedUniqueActivationCode"
        val encryptedUserAnswers: String = "EncryptedUserAnswers"

        val invalidUserAnswers = """{}"""

        val encryptor = new SavedPendingRegistrationEncryptor(mockEncryptionService)

        when(mockEncryptionService.encryptField(eqTo(Json.toJson(savedPendingRegistration.userAnswers).toString))) thenReturn encryptedUserAnswers
        when(mockEncryptionService.encryptField(eqTo(savedPendingRegistration.uniqueActivationCode))) thenReturn encryptedUniqueActivationCode

        val encryptedSavedPendingRegistration: EncryptedSavedPendingRegistration = encryptor.encryptAnswers(savedPendingRegistration)

        when(mockEncryptionService.decryptField(eqTo(encryptedUserAnswers))) thenReturn Json.toJson(invalidUserAnswers).toString
        when(mockEncryptionService.decryptField(eqTo(encryptedUniqueActivationCode))) thenReturn savedPendingRegistration.uniqueActivationCode

        val result = intercept[Exception](encryptor.decryptUserAnswers(encryptedSavedPendingRegistration))
        result.getMessage `mustBe` result.getLocalizedMessage
        verify(mockEncryptionService, times(1)).decryptField(any())
      }
    }

    ".decryptSaveForLaterAnswers" - {
      "must decrypt EncryptedSavedUserAnswers and return SavedUserAnswers" in {

        val encryptedData = "encrypted-json-data"
        val encryptedSavedUserAnswers = EncryptedSavedUserAnswers(
          journeyId = savedUserAnswers.journeyId,
          data = encryptedData,
          intermediaryNumber = savedUserAnswers.intermediaryNumber,
          lastUpdated = savedUserAnswers.lastUpdated
        )

        val originalDataAsString = Json.toJson(savedUserAnswers.data).toString
        when(mockEncryptionService.decryptField(encryptedData)).thenReturn(originalDataAsString)

        val encryptor = new SavedPendingRegistrationEncryptor(mockEncryptionService)
        val result = encryptor.decryptSaveForLaterAnswers(encryptedSavedUserAnswers)

        result `mustBe` savedUserAnswers
        result.data `mustBe` savedUserAnswers.data
        result.journeyId `mustBe` savedUserAnswers.journeyId
        result.intermediaryNumber `mustBe` savedUserAnswers.intermediaryNumber
        result.lastUpdated `mustBe` savedUserAnswers.lastUpdated

        verify(mockEncryptionService).decryptField(encryptedData)
        verify(mockEncryptionService, times(1)).decryptField(any())

      }
    }
  }
}
