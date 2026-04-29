package uk.gov.hmrc.iossnetpregistration.services

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.{PendingRegistrationRequest, SavedPendingRegistration, UserAnswers}
import uk.gov.hmrc.iossnetpregistration.repositories.PendingRegistrationRepository
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}


class SavePendingRegistrationServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockPendingRegistrationRepository: PendingRegistrationRepository = mock[PendingRegistrationRepository]
  private val mockUniqueCodeGeneratorService: UniqueCodeGeneratorService = mock[UniqueCodeGeneratorService]

  implicit val ec: ExecutionContext = ExecutionContext.global


  private val savedPendingRegistration: SavedPendingRegistration = arbitrarySavedPendingRegistration.arbitrary.sample.value

  private val userAnswers: UserAnswers = savedPendingRegistration.userAnswers

  private val pendingRegistrationRequest = PendingRegistrationRequest(userAnswers, savedPendingRegistration.intermediaryDetails)

  override def beforeEach(): Unit = {
    Mockito.reset(mockPendingRegistrationRepository)
    Mockito.reset(mockUniqueCodeGeneratorService)
  }

  def SavePendingRegistrationService: SavePendingRegistrationService = {
    new SavePendingRegistrationService(mockPendingRegistrationRepository, mockUniqueCodeGeneratorService)(ec)
  }

  "SavePendingRegistrationService" - {

    ".savePendingRegistration" - {

      "must create a new saved pending registration and save it to the pending registration repository when invoked" in {

        val testSavedPendingReg = savedPendingRegistration.copy(uniqueUrlCode = "ABCDEF", uniqueActivationCode = "ABCDEF")

        when(mockPendingRegistrationRepository.set(any())) thenReturn testSavedPendingReg.toFuture
        when(mockUniqueCodeGeneratorService.generateUniqueCode()) thenReturn "ABCDEF"

        val service = SavePendingRegistrationService
        val result = service.savePendingRegistration(pendingRegistrationRequest).futureValue

        result `mustBe` testSavedPendingReg
        verify(mockPendingRegistrationRepository, times(1)).set(eqTo(testSavedPendingReg))
        verify(mockUniqueCodeGeneratorService, times(2)).generateUniqueCode()
      }
    }

    ".getPendingRegistration" - {

      "must return a saved pending registration when a record exists for the corresponding journeyId" in {

        when(mockPendingRegistrationRepository.get(any())) thenReturn Some(savedPendingRegistration).toFuture

        val service = SavePendingRegistrationService

        val result = service.getPendingRegistration(savedPendingRegistration.journeyId).futureValue

        result `mustBe` Some(savedPendingRegistration)
        verify(mockPendingRegistrationRepository, times(1)).get(eqTo(savedPendingRegistration.journeyId))
      }

      "must return a saved pending registration when a record exists for the corresponding uniqueUrlCode" in {

        when(mockPendingRegistrationRepository.get(any())) thenReturn Some(savedPendingRegistration).toFuture

        val service = SavePendingRegistrationService

        val result = service.getPendingRegistration(savedPendingRegistration.uniqueUrlCode).futureValue

        result `mustBe` Some(savedPendingRegistration)
        verify(mockPendingRegistrationRepository, times(1)).get(eqTo(savedPendingRegistration.uniqueUrlCode))
      }

      "must return None when no records exist for a given journeyId" in {

        val nonExistentJourneyId: String = arbitrary[String].sample.value

        when(mockPendingRegistrationRepository.get(any())) thenReturn None.toFuture

        val service = SavePendingRegistrationService

        val result = service.getPendingRegistration(nonExistentJourneyId).futureValue

        result must not be defined
        verify(mockPendingRegistrationRepository, times(1)).get(eqTo(nonExistentJourneyId))
      }
    }

    ".getPendingRegistrationsByCustomerIdentification" - {

      def registrationWithData(data: JsObject): SavedPendingRegistration =
        savedPendingRegistration.copy(
          userAnswers = savedPendingRegistration.userAnswers.copy(data = data)
        )

      "must return pending registrations matching VRN" in {
        val matchingReg =
          registrationWithData(Json.obj("clientVatNumber" -> "123456789"))

        val nonMatchingReg =
          registrationWithData(Json.obj("clientVatNumber" -> "987654321"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(matchingReg, nonMatchingReg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("VRN", "123456789").futureValue

        result mustBe Seq(matchingReg)
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }

      "must return pending registrations matching NINO" in {
        val matchingReg =
          registrationWithData(Json.obj("clientsNinoNumber" -> "AB123456C"))

        val nonMatchingReg =
          registrationWithData(Json.obj("clientsNinoNumber" -> "CD123456E"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(matchingReg, nonMatchingReg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("NINO", "AB123456C").futureValue

        result mustBe Seq(matchingReg)
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }

      "must return pending registrations matching UTR" in {
        val matchingReg =
          registrationWithData(Json.obj("clientUtrNumber" -> "1234567890"))

        val nonMatchingReg =
          registrationWithData(Json.obj("clientUtrNumber" -> "0987654321"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(matchingReg, nonMatchingReg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("UTR", "1234567890").futureValue

        result mustBe Seq(matchingReg)
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }

      "must return pending registrations matching FTR" in {
        val matchingReg =
          registrationWithData(Json.obj("clientTaxRefrence" -> "XATR1234567890"))

        val nonMatchingReg =
          registrationWithData(Json.obj("clientTaxRefrence" -> "XATR0987654321"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(matchingReg, nonMatchingReg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("FTR", "XATR1234567890").futureValue

        result mustBe Seq(matchingReg)
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }

      "must return an empty sequence when idType is unsupported" in {
        val reg =
          registrationWithData(Json.obj("clientVatNumber" -> "123456789"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(reg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("INVALID", "123456789").futureValue

        result mustBe Seq.empty
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }

      "must return an empty sequence when no pending registration matches the idValue" in {
        val reg =
          registrationWithData(Json.obj("clientVatNumber" -> "987654321"))

        when(mockPendingRegistrationRepository.getAll())
          .thenReturn(Seq(reg).toFuture)

        val service = SavePendingRegistrationService

        val result =
          service.getPendingRegistrationsByCustomerIdentification("VRN", "123456789").futureValue

        result mustBe Seq.empty
        verify(mockPendingRegistrationRepository, times(1)).getAll()
      }
    }

    ".validateClientActivationCode" - {

      "must return a Some(true) when a record exists for the uniqueUrlCode and there's matching uniqueActivationCode's" in {

        when(mockPendingRegistrationRepository.getDecrypted(any())) thenReturn Some(savedPendingRegistration).toFuture

        val service = SavePendingRegistrationService

        val result: Future[Option[Boolean]] = service.validateClientActivationCode(savedPendingRegistration.uniqueUrlCode, savedPendingRegistration.uniqueActivationCode)

        whenReady(result) { result =>
          result mustEqual Some(true)
        }
        verify(mockPendingRegistrationRepository, times(1)).getDecrypted(eqTo(savedPendingRegistration.uniqueUrlCode))

      }

      "must return a Some(false) when a record exists for the uniqueUrlCode and there's mismatching uniqueActivationCode's" in {

        when(mockPendingRegistrationRepository.getDecrypted(any())) thenReturn Some(savedPendingRegistration).toFuture

        val service = SavePendingRegistrationService

        val result = service.validateClientActivationCode(savedPendingRegistration.uniqueUrlCode, "NotAMatchingCode")

        whenReady(result) { result =>
          result mustEqual Some(false)
        }
        verify(mockPendingRegistrationRepository, times(1)).getDecrypted(eqTo(savedPendingRegistration.uniqueUrlCode))
      }

      "must return None when no records exist for a given uniqueUrlCode" in {

        val nonExistentUniqueUrlCode: String = arbitrary[String].sample.value

        when(mockPendingRegistrationRepository.getDecrypted(any())) thenReturn None.toFuture

        val service = SavePendingRegistrationService

        val result = service.validateClientActivationCode(nonExistentUniqueUrlCode, savedPendingRegistration.uniqueActivationCode)

        whenReady(result) { result =>
          result mustEqual None
        }
        verify(mockPendingRegistrationRepository, times(1)).getDecrypted(eqTo(nonExistentUniqueUrlCode))
      }
    }

    ".getPendingRegistrationsByIntermediaryNumber" - {

      "must return a saved pending registration when a record exists for the corresponding intermediary number" in {

        when(mockPendingRegistrationRepository.getByIntermediaryNumber(any())) thenReturn Seq(savedPendingRegistration).toFuture

        val service = SavePendingRegistrationService

        val result = service.getPendingRegistrationsByIntermediaryNumber(savedPendingRegistration.intermediaryDetails.intermediaryNumber).futureValue

        result `mustBe` Seq(savedPendingRegistration)
        verify(mockPendingRegistrationRepository, times(1)).getByIntermediaryNumber(eqTo(savedPendingRegistration.intermediaryDetails.intermediaryNumber))
      }

      "must return a multiple saved pending registrations when more than one record exists for the corresponding intermediary number" in {
        val savedPendingRegistration1 = arbitrarySavedPendingRegistration.arbitrary.sample.value
        val savedPendingRegistration2 = arbitrarySavedPendingRegistration.arbitrary.sample.value
        val savedPendingRegistration3 = arbitrarySavedPendingRegistration.arbitrary.sample.value

        val expected = Seq(savedPendingRegistration1, savedPendingRegistration2, savedPendingRegistration3)

        when(mockPendingRegistrationRepository.getByIntermediaryNumber(any()))
          .thenReturn(expected.toFuture)

        val service = SavePendingRegistrationService

        val result = service
          .getPendingRegistrationsByIntermediaryNumber(savedPendingRegistration1.intermediaryDetails.intermediaryNumber)
          .futureValue

        result mustBe expected
        verify(mockPendingRegistrationRepository, times(1)).getByIntermediaryNumber(eqTo(savedPendingRegistration1.intermediaryDetails.intermediaryNumber))
      }

      "must return an empty list when no records exist for a given intermediary number" in {

        val nonExistentIntermediaryNumber: String = arbitrary[String].sample.value

        when(mockPendingRegistrationRepository.getByIntermediaryNumber(any())) thenReturn Seq.empty[SavedPendingRegistration].toFuture

        val service = SavePendingRegistrationService

        val result = service.getPendingRegistrationsByIntermediaryNumber(nonExistentIntermediaryNumber).futureValue

        result mustBe Seq.empty
        verify(mockPendingRegistrationRepository, times(1)).getByIntermediaryNumber(eqTo(nonExistentIntermediaryNumber))
      }
    }

    ".deletePendingRegistration" - {

      "must delete a pending registration for a given journeyId" in {

        when(mockPendingRegistrationRepository.delete(userAnswers.journeyId)) thenReturn true.toFuture

        val service = SavePendingRegistrationService

        val result = service.deletePendingRegistration(userAnswers.journeyId).futureValue

        result `mustBe` true
        verify(mockPendingRegistrationRepository, times(1)).delete(eqTo(userAnswers.journeyId))
      }
    }

    ".count" - {

      "must return a count of pending registrations" in {

        when(mockPendingRegistrationRepository.count(any())) thenReturn 4.toLong.toFuture

        val service = SavePendingRegistrationService

        val result = service.getCount(iossNumber).futureValue

        result `mustBe` 4
        verify(mockPendingRegistrationRepository, times(1)).count(eqTo(iossNumber))
      }
    }

    ".updateClientEmailAddress" - {

      val journeyId = savedPendingRegistration.journeyId
      val oldEmail = "old@email.com"
      val newEmail = "new@email.com"

      val savedPendingRegistrationWithOldEmail: SavedPendingRegistration =
        savedPendingRegistration.copy(
          userAnswers = savedPendingRegistration.userAnswers.copy(
            data = Json.obj(
              "businessContactDetails" -> Json.obj(
                "emailAddress" -> oldEmail
              )
            )
          )
        )

      "must update an email address of a given journeyId if it exists in the database" in {

        val updatedPendingRegistration =
          savedPendingRegistrationWithOldEmail.copy(
            userAnswers = savedPendingRegistrationWithOldEmail.userAnswers.copy(
              data = Json.obj(
                "businessContactDetails" -> Json.obj(
                  "emailAddress" -> newEmail
                )
              )
            )
          )

        when(mockPendingRegistrationRepository.getDecrypted(eqTo(journeyId))) thenReturn Some(savedPendingRegistrationWithOldEmail).toFuture
        when(mockPendingRegistrationRepository.updateClientEmail(any())) thenReturn updatedPendingRegistration.toFuture

        val service = SavePendingRegistrationService

        val result = service.updateClientEmailAddress(journeyId, newEmail).futureValue

        result.value.userAnswers.data mustBe updatedPendingRegistration.userAnswers.data
        (result.value.userAnswers.data \ "businessContactDetails" \ "emailAddress").as[String] mustBe newEmail

        verify(mockPendingRegistrationRepository, times(1)).getDecrypted(eqTo(journeyId))
        verify(mockPendingRegistrationRepository, times(1)).updateClientEmail(any())
      }

      "must return None when no record exists for the given journeyId" in {

        when(mockPendingRegistrationRepository.getDecrypted(eqTo(journeyId))).thenReturn(None.toFuture)

        val service = SavePendingRegistrationService

        val result = service.updateClientEmailAddress(journeyId, newEmail).futureValue

        result mustBe None
        verify(mockPendingRegistrationRepository, times(1)).getDecrypted(eqTo(journeyId))
        verify(mockPendingRegistrationRepository, never()).updateClientEmail(any())
      }
    }
  }
}
