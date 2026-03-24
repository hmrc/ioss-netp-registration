package uk.gov.hmrc.iossnetpregistration.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{CREATED, GET, POST, contentAsJson, defaultAwaitTimeout, route, running, status, writeableOf_AnyContentAsJson}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.SavedUserAnswers
import uk.gov.hmrc.iossnetpregistration.services.SaveForLaterService
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps


class SaveForLaterControllerSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockSaveForLaterService: SaveForLaterService = mock[SaveForLaterService]

  private val savedUserAnswers: SavedUserAnswers = arbitrarySavedUserAnswers.arbitrary.sample.value

  private val journeyId = "JourneyId123"
  private val intermediaryNumber = "IM12345"


  private lazy val saveForLaterPostRoute: String = routes.SaveForLaterController.post().url
  private lazy val saveForLaterGetRoute: String = routes.SaveForLaterController.get(journeyId).url
  private lazy val saveForLaterSelectionGetRoute: String = routes.SaveForLaterController.getSelection(intermediaryNumber).url
  private lazy val saveForLaterDeleteRoute: String = routes.SaveForLaterController.delete(journeyId).url
  private lazy val saveForLaterGetCount: String = routes.SaveForLaterController.getCount(intermediaryNumber).url

  private val saveForLaterRequest = arbitrarySaveForLaterRequest.arbitrary.sample.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockSaveForLaterService)
  }

  "SaveForLaterController" - {
    ".post" - {
      "must save a SaveForLaterRequest and respond with Created when successful" in {

        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.saveUserAnswers(any())) thenReturn savedUserAnswers.toFuture

        running(application) {

          val request = FakeRequest(POST, saveForLaterPostRoute)
            .withJsonBody(Json.toJson(saveForLaterRequest))

          val result = route(application, request).value

          status(result) mustEqual CREATED
          contentAsJson(result) mustEqual Json.toJson(savedUserAnswers)
          verify(mockSaveForLaterService, times(1)).saveUserAnswers(eqTo(saveForLaterRequest))
        }

      }
    }
    ".get" - {

      "must respond with OK and a savedUserAnswers for the matching journeyID" in {
        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.getSavedUserAnswers(any())(any())) thenReturn Some(savedUserAnswers).toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterGetRoute)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(savedUserAnswers)
          verify(mockSaveForLaterService, times(1)).getSavedUserAnswers(any())(any())
        }
      }
      "must return an internalServerError if no savedUserAnswers found" in {

        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.getSavedUserAnswers(any())(any())) thenReturn None.toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterGetRoute)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) mustEqual INTERNAL_SERVER_ERROR
          verify(mockSaveForLaterService, times(1)).getSavedUserAnswers(any())(any())
        }

      }
    }
    ".getSelection" - {
      "must respond with OK and a seq of savedUserAnswers" in {
        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.getSavedUserAnswersSelection(any())(any())) thenReturn Seq(savedUserAnswers).toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterSelectionGetRoute)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(Seq(savedUserAnswers))
          verify(mockSaveForLaterService, times(1)).getSavedUserAnswersSelection(any())(any())
        }
      }
    }
    ".getCount" - {

      "must return a count of pending registrations" in {

        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.getCount(any())) thenReturn 4.toLong.toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterGetCount)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(4)
          verify(mockSaveForLaterService, times(1)).getCount(eqTo(intermediaryNumber))
        }
      }
    }
    ".delete" - {
      "must respond with Ok and a boolean true when delete was successful" in {
        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.deleteSavedUserAnswers(any())) thenReturn true.toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterDeleteRoute)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(true)
          verify(mockSaveForLaterService, times(1)).deleteSavedUserAnswers(any())
        }

      }
      "must respond with Ok and a boolean false when delete was successful" in {
        val application = applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockSaveForLaterService))
          .build()

        when(mockSaveForLaterService.deleteSavedUserAnswers(any())) thenReturn false.toFuture

        running(application) {

          val request = FakeRequest(GET, saveForLaterDeleteRoute)
            .withJsonBody(Json.toJson(""))

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsJson(result) mustEqual Json.toJson(false)
          verify(mockSaveForLaterService, times(1)).deleteSavedUserAnswers(any())
        }
      }
    }
  }
}
