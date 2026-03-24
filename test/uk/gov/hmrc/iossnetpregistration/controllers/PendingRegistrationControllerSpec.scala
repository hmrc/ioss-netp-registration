package uk.gov.hmrc.iossnetpregistration.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{DELETE, GET, POST, PUT, contentAsJson, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsJson}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.{PendingRegistrationRequest, SavedPendingRegistration, UserAnswers}
import uk.gov.hmrc.iossnetpregistration.services.{SavePendingRegistrationService, UniqueCodeGeneratorService}
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps


class PendingRegistrationControllerSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockSavePendingRegistrationService: SavePendingRegistrationService = mock[SavePendingRegistrationService]

  private val mockUniqueCodeGeneratorService: UniqueCodeGeneratorService = mock[UniqueCodeGeneratorService]
  when(mockUniqueCodeGeneratorService.generateUniqueCode()) thenReturn arbitrarySavedPendingRegistration.arbitrary.sample.value.uniqueUrlCode

  private val savedPendingRegistration: SavedPendingRegistration = arbitrarySavedPendingRegistration.arbitrary.sample.value

  private val userAnswers: UserAnswers = savedPendingRegistration.userAnswers

  private val pendingRegistrationRequest = PendingRegistrationRequest(userAnswers, savedPendingRegistration.intermediaryDetails)

  private lazy val pendingRegistrationPostRoute: String = routes.PendingRegistrationController.post().url

  def pendingRegistrationGetRoute(journeyIdOrUrlCode: String): String = routes.PendingRegistrationController.get(journeyIdOrUrlCode).url

  def pendingRegistrationsByIntNumberGetRoute(intermediaryNumber: String): String = routes.PendingRegistrationController.getByIntermediaryNumber(intermediaryNumber).url

  def numberOfPendingRegistrationsGetRoute(intermediaryNumber: String): String = routes.PendingRegistrationController.getCount(intermediaryNumber).url

  def pendingRegistrationValidateRoute(uniqueUrlCode: String, uniqueActivationCode: String): String = routes.PendingRegistrationController.validate(uniqueUrlCode, uniqueActivationCode).url

  def deletePendingRegistrationRoute(journeyId: String): String = routes.PendingRegistrationController.delete(journeyId).url

  def updateClientEmailAddressRoute(journeyId: String, newEmailAddress: String): String = routes.PendingRegistrationController.updateClientEmailAddress(journeyId, newEmailAddress).url

  override def beforeEach(): Unit = {
    Mockito.reset(mockSavePendingRegistrationService)
  }

  "PendingRegistrationController" - {

    ".post" - {

      "must save a Saved Pending Registration and respond with Created when successful" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.savePendingRegistration(any())) thenReturn savedPendingRegistration.toFuture

        running(application) {

          val request = FakeRequest(POST, pendingRegistrationPostRoute)
            .withJsonBody(Json.toJson(pendingRegistrationRequest))

          val result = route(application, request).value

          status(result) mustEqual CREATED
          contentAsJson(result) mustEqual Json.toJson(savedPendingRegistration)
          verify(mockSavePendingRegistrationService, times(1)).savePendingRegistration(eqTo(pendingRegistrationRequest))
        }
      }
    }

    ".get" - {

      "must retrieve a pending registration for a given journeyId when one exists" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getPendingRegistration(any())) thenReturn Some(savedPendingRegistration).toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationGetRoute(savedPendingRegistration.journeyId))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(savedPendingRegistration)
          verify(mockSavePendingRegistrationService, times(1)).getPendingRegistration(eqTo(savedPendingRegistration.journeyId))
        }
      }

      "must retrieve a pending registration for a given uniqueUrlCode when one exists" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getPendingRegistration(any())) thenReturn Some(savedPendingRegistration).toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationGetRoute(savedPendingRegistration.uniqueUrlCode))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(savedPendingRegistration)
          verify(mockSavePendingRegistrationService, times(1)).getPendingRegistration(eqTo(savedPendingRegistration.uniqueUrlCode))
        }
      }

      "must return an Internal Server Error when a pending registration record does not exist for a given journeyId" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getPendingRegistration(any())) thenReturn None.toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationGetRoute(savedPendingRegistration.journeyId))

          val result = route(application, request).value

          status(result) `mustBe` INTERNAL_SERVER_ERROR
          verify(mockSavePendingRegistrationService, times(1)).getPendingRegistration(eqTo(savedPendingRegistration.journeyId))
        }
      }
    }

    ".validate" - {

      "must return a boolean when a uniqueUrlCode and matching uniqueActivationCode are provided" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.validateClientActivationCode(any(), any())) thenReturn Some(true).toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationValidateRoute(savedPendingRegistration.uniqueUrlCode, savedPendingRegistration.uniqueActivationCode))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(true)
          verify(mockSavePendingRegistrationService, times(1)).validateClientActivationCode(eqTo(savedPendingRegistration.uniqueUrlCode), eqTo(savedPendingRegistration.uniqueActivationCode))
        }
      }

      "must return an Internal Server Error when any error occurs whilst validating" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.validateClientActivationCode(any(), any())) thenReturn None.toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationValidateRoute(savedPendingRegistration.uniqueUrlCode, savedPendingRegistration.uniqueActivationCode))

          val result = route(application, request).value

          status(result) `mustBe` INTERNAL_SERVER_ERROR
          verify(mockSavePendingRegistrationService, times(1)).validateClientActivationCode(eqTo(savedPendingRegistration.uniqueUrlCode), eqTo(savedPendingRegistration.uniqueActivationCode))
        }
      }

    }

    ".getByIntermediaryNumber" - {

      "must retrieve a pending registration for a given intermediary number when one exists" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getPendingRegistrationsByIntermediaryNumber(any())) thenReturn Seq(savedPendingRegistration).toFuture

        running(application) {

          val request = FakeRequest(GET, pendingRegistrationsByIntNumberGetRoute(savedPendingRegistration.intermediaryDetails.intermediaryNumber))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(Seq(savedPendingRegistration))
          verify(mockSavePendingRegistrationService, times(1)).getPendingRegistrationsByIntermediaryNumber(eqTo(savedPendingRegistration.intermediaryDetails.intermediaryNumber))
        }
      }

      "must retrieve multiple pending registrations for a given intermediary number when more than one exists" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        val record1 = savedPendingRegistration
        val record2 = arbitrarySavedPendingRegistration.arbitrary.sample.value
          .copy(intermediaryDetails = record1.intermediaryDetails)

        when(mockSavePendingRegistrationService.getPendingRegistrationsByIntermediaryNumber(any()))
          .thenReturn(Seq(record1, record2).toFuture)

        running(application) {
          val request = FakeRequest(GET, pendingRegistrationsByIntNumberGetRoute(record1.intermediaryDetails.intermediaryNumber))

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsJson(result) mustBe Json.toJson(Seq(record1, record2))
          verify(mockSavePendingRegistrationService, times(1))
            .getPendingRegistrationsByIntermediaryNumber(eqTo(record1.intermediaryDetails.intermediaryNumber))
        }
      }

      "must return an empty list when a pending registration record does not exist for a given intermediary number" in {

        val nonExistentIntermediaryNumber: String = arbitrary[String].sample.value

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getPendingRegistrationsByIntermediaryNumber(any()))
          .thenReturn(Seq.empty[SavedPendingRegistration].toFuture)

        running(application) {
          val request = FakeRequest(GET, pendingRegistrationsByIntNumberGetRoute(nonExistentIntermediaryNumber))

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsJson(result) mustBe Json.toJson(Seq.empty[SavedPendingRegistration])
          verify(mockSavePendingRegistrationService, times(1)).getPendingRegistrationsByIntermediaryNumber(eqTo(nonExistentIntermediaryNumber))
        }
      }
    }

    ".getCount" - {

      "must return a count of pending registrations" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.getCount(any())) thenReturn 4.toLong.toFuture

        running(application) {

          val request = FakeRequest(GET, numberOfPendingRegistrationsGetRoute(iossNumber))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(4)
          verify(mockSavePendingRegistrationService, times(1)).getCount(eqTo(iossNumber))
        }
      }
    }

    ".delete" - {

      "must delete a pending registration for a given journeyId" in {

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.deletePendingRegistration(any())) thenReturn true.toFuture

        running(application) {

          val request = FakeRequest(DELETE, deletePendingRegistrationRoute(userAnswers.journeyId))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(true)
          verify(mockSavePendingRegistrationService, times(1)).deletePendingRegistration(eqTo(userAnswers.journeyId))
        }
      }
    }

    ".updateClientEmailAddress" - {

      val journeyId = savedPendingRegistration.journeyId

      "must update the email address for a given journeyId when one exists" in {
        
        val newEmail = "new@email.com"
        val requestBody = Json.obj("emailAddress" -> newEmail)

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.updateClientEmailAddress(eqTo(journeyId), eqTo(newEmail))) thenReturn
          Some(savedPendingRegistration).toFuture

        running(application) {

          val request = FakeRequest(PUT, updateClientEmailAddressRoute(journeyId, newEmail))
            .withBody(requestBody)

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(savedPendingRegistration)
          verify(mockSavePendingRegistrationService, times(1))
            .updateClientEmailAddress(eqTo(savedPendingRegistration.journeyId), eqTo(newEmail))
        }
      }

      "must return a 404 NonFound error when no record exists for a given journeyId" in {
        
        val newEmail = "doesnotexist@email.com"
        val requestBody = Json.obj("emailAddress" -> newEmail)

        val application = applicationBuilder()
          .overrides(bind[SavePendingRegistrationService].toInstance(mockSavePendingRegistrationService))
          .build()

        when(mockSavePendingRegistrationService.updateClientEmailAddress(journeyId, newEmail)) thenReturn None.toFuture

        running(application) {

          val request = FakeRequest(PUT, updateClientEmailAddressRoute(journeyId, newEmail))
            .withBody(requestBody)

          val result = route(application, request).value

          status(result) `mustBe` NOT_FOUND
          verify(mockSavePendingRegistrationService, times(1))
            .updateClientEmailAddress(eqTo(savedPendingRegistration.journeyId), eqTo(newEmail))
        }
      }
    }
  }
}
