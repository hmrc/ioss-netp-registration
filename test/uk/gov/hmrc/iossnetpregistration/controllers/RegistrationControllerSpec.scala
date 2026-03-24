package uk.gov.hmrc.iossnetpregistration.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.connectors.EnrolmentsConnector
import uk.gov.hmrc.iossnetpregistration.models.requests.ClientIdentifierRequest
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.audit.{EtmpAmendRegistrationAuditModel, EtmpRegistrationAuditType, EtmpRegistrationRequestAuditModel, SubmissionResult}
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpRegistrationStatus
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.RegistrationWrapper
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.{AmendRegistrationResponse, EtmpAmendRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.EtmpEnrolmentResponse
import uk.gov.hmrc.iossnetpregistration.models.responses.{EtmpEnrolmentError, EtmpException, ServiceUnavailable}
import uk.gov.hmrc.iossnetpregistration.repositories.InsertResult.InsertSucceeded
import uk.gov.hmrc.iossnetpregistration.repositories.RegistrationStatusRepository
import uk.gov.hmrc.iossnetpregistration.services.{AuditService, RegistrationService, RetryService}
import uk.gov.hmrc.iossnetpregistration.testutils.RegistrationData
import uk.gov.hmrc.iossnetpregistration.testutils.RegistrationData.etmpRegistrationRequest
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps

import java.time.LocalDateTime
import scala.concurrent.Future

class RegistrationControllerSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockRegistrationService: RegistrationService = mock[RegistrationService]
  private val mockEnrolmentsConnector: EnrolmentsConnector = mock[EnrolmentsConnector]
  private val mockRegistrationStatusRepository: RegistrationStatusRepository = mock[RegistrationStatusRepository]
  private val mockRetryService: RetryService = mock[RetryService]
  private val mockAuditService: AuditService = mock[AuditService]

  private lazy val createRegistrationRoute: String = routes.RegistrationController.createRegistration().url
  private lazy val displayRegistrationRoute: String = routes.RegistrationController.displayRegistration(iossNumber).url

  private lazy val amendRoute: String = routes.RegistrationController.amend().url

  override def beforeEach(): Unit = {
    reset(mockRegistrationService)
    reset(mockEnrolmentsConnector)
    reset(mockRegistrationStatusRepository)
    reset(mockRetryService)
    reset(mockAuditService)

    super.beforeEach()
  }

  ".createRegistration" - {

    "must audit the event and return CREATED with a response payload when given a valid payload and the registration is created successfully" in {

      val fbNumber = "123456789"

      val etmpEnrolmentResponse =
        EtmpEnrolmentResponse(
          processingDateTime = LocalDateTime.now(stubClock),
          formBundleNumber = fbNumber,
          iossReference = "test",
          businessPartner = "test businessPartner"
        )

      when(mockRegistrationService.createRegistration(eqTo(etmpRegistrationRequest))) thenReturn Right(etmpEnrolmentResponse).toFuture
      when(mockRegistrationStatusRepository.delete(eqTo(fbNumber))) thenReturn true.toFuture
      when(mockRegistrationStatusRepository.insert(any())) thenReturn InsertSucceeded.toFuture
      when(mockEnrolmentsConnector.confirmEnrolment(any())(any())) thenReturn HttpResponse(204, "").toFuture
      when(mockRetryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn EtmpRegistrationStatus.Success.toFuture
      doNothing().when(mockAuditService).audit(any())(any(), any())

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[EnrolmentsConnector].toInstance(mockEnrolmentsConnector))
        .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
        .overrides(bind[RetryService].toInstance(mockRetryService))
        .overrides(bind[AuditService].toInstance(mockAuditService))
        .build()

      running(app) {
        val request = FakeRequest(POST, createRegistrationRoute)
          .withJsonBody(Json.toJson(etmpRegistrationRequest))

        val result = route(app, request).value

        implicit val dataRequest: ClientIdentifierRequest[AnyContentAsJson] =
          ClientIdentifierRequest(request, "12345-credId", Enrolments(Set.empty))

        val expectedAuditEvent = EtmpRegistrationRequestAuditModel.build(
          etmpRegistrationAuditType = EtmpRegistrationAuditType.CreateRegistration,
          etmpRegistrationRequest = etmpRegistrationRequest,
          etmpEnrolmentResponse = Some(etmpEnrolmentResponse),
          etmpAmendResponse = None,
          errorResponse = None,
          submissionResult = SubmissionResult.Success
        )

        status(result) mustBe CREATED
        contentAsJson(result) mustBe Json.toJson(etmpEnrolmentResponse)
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }

    "must throw exception when enrolment is not confirmed" in {

      val fbNumber = "123456789"

      val etmpEnrolmentResponse =
        EtmpEnrolmentResponse(
          processingDateTime = LocalDateTime.now(stubClock),
          formBundleNumber = fbNumber,
          iossReference = "test",
          businessPartner = "test businessPartner"
        )

      when(mockRegistrationService.createRegistration(eqTo(etmpRegistrationRequest))) thenReturn Right(etmpEnrolmentResponse).toFuture
      when(mockRegistrationStatusRepository.delete(eqTo(fbNumber))) thenReturn true.toFuture
      when(mockRegistrationStatusRepository.insert(any())) thenReturn InsertSucceeded.toFuture
      when(mockEnrolmentsConnector.confirmEnrolment(any())(any())) thenReturn HttpResponse(204, "").toFuture
      when(mockRetryService.getEtmpRegistrationStatus(any(), any(), any())) thenReturn EtmpRegistrationStatus.Error.toFuture

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[EnrolmentsConnector].toInstance(mockEnrolmentsConnector))
        .overrides(bind[RegistrationStatusRepository].toInstance(mockRegistrationStatusRepository))
        .overrides(bind[RetryService].toInstance(mockRetryService))
        .build()

      running(app) {
        val request = FakeRequest(POST, createRegistrationRoute)
          .withJsonBody(Json.toJson(etmpRegistrationRequest))

        val result = route(app, request).value

        whenReady(result.failed, Timeout(Span(30, Seconds))) { exp =>
          exp mustBe EtmpException(s"Failed to add enrolment, got registration status Error")
        }
      }
    }

    "must audit the event and return Conflict when the error response is a Left EtmpEnrolmentError with error code 007" in {

      val etmpEnrolmentError = EtmpEnrolmentError("007", "test error")

      when(mockRegistrationService.createRegistration(eqTo(etmpRegistrationRequest))) thenReturn Left(etmpEnrolmentError).toFuture
      doNothing().when(mockAuditService).audit(any())(any(), any())

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[AuditService].toInstance(mockAuditService))
        .build()

      running(app) {

        val request = FakeRequest(POST, createRegistrationRoute)
          .withJsonBody(Json.toJson(etmpRegistrationRequest))

        val result = route(app, request).value

        implicit val dataRequest: ClientIdentifierRequest[AnyContentAsJson] =
          ClientIdentifierRequest(request, "12345-credId", Enrolments(Set.empty))

        val expectedAuditEvent = EtmpRegistrationRequestAuditModel.build(
          etmpRegistrationAuditType = EtmpRegistrationAuditType.CreateRegistration,
          etmpRegistrationRequest = etmpRegistrationRequest,
          etmpEnrolmentResponse = None,
          etmpAmendResponse = None,
          errorResponse = Some(etmpEnrolmentError.body),
          submissionResult = SubmissionResult.Duplicate
        )

        status(result) mustBe CONFLICT
        contentAsJson(result) mustBe Json.toJson(
          s"Business Partner already has an active IOSS Subscription for this regime with error code ${etmpEnrolmentError.code}" +
          s"with message body ${etmpEnrolmentError.body}"
        )
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }

    "must audit the event and return INTERNAL_SERVER_ERROR when there is any other error response" in {

      when(mockRegistrationService.createRegistration(eqTo(etmpRegistrationRequest))) thenReturn Left(ServiceUnavailable).toFuture
      doNothing().when(mockAuditService).audit(any())(any(), any())

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[AuditService].toInstance(mockAuditService))
        .build()

      running(app) {

        val request = FakeRequest(POST, createRegistrationRoute)
          .withJsonBody(Json.toJson(etmpRegistrationRequest))

        val result = route(app, request).value

        implicit val dataRequest: ClientIdentifierRequest[AnyContentAsJson] =
          ClientIdentifierRequest(request, "12345-credId", Enrolments(Set.empty))

        val expectedAuditEvent = EtmpRegistrationRequestAuditModel.build(
          etmpRegistrationAuditType = EtmpRegistrationAuditType.CreateRegistration,
          etmpRegistrationRequest = etmpRegistrationRequest,
          etmpEnrolmentResponse = None,
          etmpAmendResponse = None,
          errorResponse = Some(ServiceUnavailable.body),
          submissionResult = SubmissionResult.Failure
        )

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsJson(result) mustBe Json.toJson(s"Internal server error ${ServiceUnavailable.body}")
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }
  }

  ".amend" - {

    "must return OK with JSON response when service succeeds" in {
      val amendRegistrationResponse = AmendRegistrationResponse(
        processingDateTime = LocalDateTime.now(stubClock),
        formBundleNumber = "123456789",
        iossReference = "test",
        businessPartner = "test businessPartner"
      )

      when(mockRegistrationService.amendRegistration(eqTo(RegistrationData.etmpAmendRegistrationRequest))) thenReturn amendRegistrationResponse.toFuture

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .build()

      running(app) {
        val request = FakeRequest(POST, amendRoute)
          .withJsonBody(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

        val result = route(app, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(amendRegistrationResponse)
      }
    }

    "must return INTERNAL_SERVER_ERROR when service throws EtmpException" in {
      val exception = EtmpException("Test error message")

      when(mockRegistrationService.amendRegistration(eqTo(RegistrationData.etmpAmendRegistrationRequest))) thenReturn Future.failed(exception)

      val app = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .build()

      running(app) {
        val request = FakeRequest(POST, amendRoute)
          .withJsonBody(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

        val result = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsJson(result) mustBe Json.toJson(s"Internal server error: ${exception.getMessage}")
      }
    }

    "must return BAD_REQUEST when given invalid JSON" in {
      val app = applicationBuilder().build()

      running(app) {
        val request = FakeRequest(POST, amendRoute)
          .withJsonBody(Json.parse(RegistrationData.invalidRegistration))

        val result = route(app, request).value

        status(result) mustBe BAD_REQUEST
      }
    }

    "must return INTERNAL_SERVER_ERROR when service throws unexpected exception" in {
      val exception = new RuntimeException("Unexpected error")

      when(mockRegistrationService.amendRegistration(eqTo(RegistrationData.etmpAmendRegistrationRequest))) thenReturn Future.failed(exception)

      val app = applicationBuilder().overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .build()

      running(app) {
        val request = FakeRequest(POST, amendRoute)
          .withJsonBody(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

        val result = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsJson(result) mustBe Json.toJson(s"Internal server error: ${exception.getMessage}")
      }
    }

    "must audit successfully when registration is amended" in {

      val amendRegistrationResponse = AmendRegistrationResponse(
        processingDateTime = LocalDateTime.now(stubClock),
        formBundleNumber = "123456789",
        iossReference = "test",
        businessPartner = "test businessPartner"
      )

      val etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest = RegistrationData.etmpAmendRegistrationRequest
      val responseJson = Json.toJson(amendRegistrationResponse)

      when(mockRegistrationService.amendRegistration(any())) thenReturn amendRegistrationResponse.toFuture
      doNothing().when(mockAuditService).audit(any())(any(), any())

      val application = applicationBuilder().overrides(
        bind[RegistrationService].toInstance(mockRegistrationService),
        bind[AuditService].toInstance(mockAuditService)
      ).build()

      running(application) {
        val request = FakeRequest(POST, amendRoute).withJsonBody(Json.toJson(etmpAmendRegistrationRequest))

        val result = route(application, request).value

        implicit val dataRequest: ClientIdentifierRequest[AnyContentAsJson] =
          ClientIdentifierRequest(request, "12345-credId", Enrolments(Set.empty))

        val expectedAuditEvent = EtmpAmendRegistrationAuditModel.build(
          etmpRegistrationAuditType = EtmpRegistrationAuditType.AmendRegistration,
          etmpRegistrationRequest = etmpAmendRegistrationRequest,
          etmpEnrolmentResponse = None,
          etmpAmendResponse = Some(amendRegistrationResponse),
          errorResponse = None,
          submissionResult = SubmissionResult.Success
        )

        status(result) `mustBe` OK
        contentAsJson(result) `mustBe` responseJson
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }

    "must audit failure when amendRegistration fails" in {

      val etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest = RegistrationData.etmpAmendRegistrationRequest

      when(mockRegistrationService.amendRegistration(any())) thenReturn Future.failed(Exception("Error"))
      doNothing().when(mockAuditService).audit(any())(any(), any())

      val application = applicationBuilder().overrides(
        bind[RegistrationService].toInstance(mockRegistrationService),
        bind[AuditService].toInstance(mockAuditService)
      ).build()

      running(application) {
        val request = FakeRequest(POST, amendRoute).withJsonBody(Json.toJson(etmpAmendRegistrationRequest))

        val result = route(application, request).value

        implicit val dataRequest: ClientIdentifierRequest[AnyContentAsJson] =
          ClientIdentifierRequest(request, "12345-credId", Enrolments(Set.empty))

        val expectedErrorMessage = "Error"

        val expectedAuditEvent = EtmpAmendRegistrationAuditModel.build(
          etmpRegistrationAuditType = EtmpRegistrationAuditType.AmendRegistration,
          etmpRegistrationRequest = etmpAmendRegistrationRequest,
          etmpEnrolmentResponse = None,
          etmpAmendResponse = None,
          errorResponse = Some(expectedErrorMessage),
          submissionResult = SubmissionResult.Failure
        )

        status(result) `mustBe` INTERNAL_SERVER_ERROR
        verify(mockAuditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
      }
    }
  }

  ".displayRegistration" - {

    val registrationWrapper: RegistrationWrapper = arbitraryRegistrationWrapper.arbitrary.sample.value

    "must return OK with a RegistrationWrapper JSON payload when one is successfully retrieved" in {

      when(mockRegistrationService.getRegistration(any())(any())) thenReturn registrationWrapper.toFuture

      val application = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .build()

      val jsonRegistrationWrapper = Json.toJson(registrationWrapper)

      running(application) {

        val request = FakeRequest(GET, displayRegistrationRoute)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsJson(result) `mustBe` jsonRegistrationWrapper
        verify(mockRegistrationService, times(1)).getRegistration(eqTo(iossNumber))(any())
      }
    }

    "must return an Internal Server Error when the server throws an error" in {

      when(mockRegistrationService.getRegistration(any())(any())) thenReturn Future.failed(EtmpException("ERROR"))

      val application = applicationBuilder()
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .build()

      running(application) {

        val request = FakeRequest(GET, displayRegistrationRoute)

        val result = route(application, request).value

        status(result) `mustBe` INTERNAL_SERVER_ERROR
        verify(mockRegistrationService, times(1)).getRegistration(eqTo(iossNumber))(any())
      }
    }
  }
}