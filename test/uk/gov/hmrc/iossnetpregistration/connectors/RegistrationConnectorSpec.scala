package uk.gov.hmrc.iossnetpregistration.connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.http.HeaderNames.{AUTHORIZATION, CONTENT_TYPE}
import play.api.http.MimeTypes
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.connectors.RegistrationHttpParser.serviceName
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.binders.Format.eisDateTimeFormatter
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.EtmpDisplayRegistration
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.AmendRegistrationResponse
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse, EtmpErrorDetail}
import uk.gov.hmrc.iossnetpregistration.models.responses.*
import uk.gov.hmrc.iossnetpregistration.testutils.RegistrationData
import uk.gov.hmrc.iossnetpregistration.testutils.RegistrationData.etmpRegistrationRequest

import java.time.LocalDateTime

class RegistrationConnectorSpec extends BaseSpec with WireMockHelper {

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.create-registration.host" -> "127.0.0.1",
        "microservice.services.create-registration.port" -> server.port,
        "microservice.services.create-registration.authorizationToken" -> "auth-token",
        "microservice.services.create-registration.environment" -> "test-environment",
        "microservice.services.display-registration.host" -> "127.0.0.1",
        "microservice.services.display-registration.port" -> server.port,
        "microservice.services.display-registration.authorizationToken" -> "auth-token",
        "microservice.services.display-registration.environment" -> "test-environment"
      )
      .build()

  private val createRegistrationUrl = "/ioss-netp-registration-stub/vec/iosssubscription/subdatatransfer/v1"
  private val amendRegistrationUrl = "/ioss-netp-registration-stub/vec/iossregistration/amendregistration/v1"
  private val getRegistrationUrl = s"/ioss-netp-registration-stub/vec/iossregistration/viewreg/v1/$iossNumber"

  private val fixedDelay = 21000

  private val timeOutSpan = 30

  ".createRegistration" - {

    "should return an ETMP Enrolment Response correctly" in {

      val now = LocalDateTime.now()

      val formBundleNumber = arbitrary[String].sample.value
      val iossReference = arbitraryVatNumberTraderId.arbitrary.sample.value.vatNumber
      val businessPartner = arbitrary[String].sample.value

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(CREATED)
            .withBody(Json.stringify(Json.toJson(
              EtmpEnrolmentResponse(now, formBundleNumber, iossReference, businessPartner)))))

      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.createRegistration(etmpRegistrationRequest).futureValue

        result mustBe Right(EtmpEnrolmentResponse(now, formBundleNumber, iossReference, businessPartner))
      }
    }

    "should return Invalid Json when server responds with InvalidJson" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(CREATED)
            .withBody(Json.stringify(Json.toJson("tests" -> "invalid"))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.createRegistration(etmpRegistrationRequest).futureValue
        result mustBe Left(InvalidJson)
      }
    }

    "should return EtmpError when server responds with status 422 and correct error response json" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      val errorResponse = EtmpEnrolmentErrorResponse(EtmpErrorDetail(LocalDateTime.now(stubClock).format(eisDateTimeFormatter), Some("123"), Some("error")))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(UNPROCESSABLE_ENTITY)
            .withBody(Json.stringify(Json.toJson(errorResponse))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.createRegistration(etmpRegistrationRequest).futureValue
        result mustBe Left(EtmpEnrolmentError("123", "error"))
      }
    }

    "should return Invalid Json when server responds with status 422 and incorrect error response json" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(UNPROCESSABLE_ENTITY)
            .withBody(Json.stringify(Json.toJson("tests" -> "invalid"))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.createRegistration(etmpRegistrationRequest).futureValue
        result mustBe Left(UnexpectedResponseStatus(UNPROCESSABLE_ENTITY, "Unexpected response from etmp registration, received status 422"))
      }
    }

    Seq((NOT_FOUND, NotFound), (CONFLICT, Conflict), (INTERNAL_SERVER_ERROR, ServerError), (BAD_REQUEST, InvalidVrn), (SERVICE_UNAVAILABLE, ServiceUnavailable), (123, UnexpectedResponseStatus(123, s"Unexpected response from ${serviceName}, received status 123")))
      .foreach { error =>
        s"should return correct error response when server responds with ${error._1}" in {

          val app = application

          val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

          server.stubFor(
            post(urlEqualTo(createRegistrationUrl))
              .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
              .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
              .withRequestBody(equalTo(requestJson))
              .willReturn(aResponse().withStatus(error._1))
          )

          running(app) {
            val connector = app.injector.instanceOf[RegistrationConnector]
            val result = connector.createRegistration(etmpRegistrationRequest).futureValue
            result mustBe Left(UnexpectedResponseStatus(error._1, s": Unexpected response from etmp registration, received status ${error._1}"))
          }
        }
      }

    "should return Error Response when server responds with Http Exception" in {

      val app = application

      val requestJson = Json.stringify(Json.toJson(etmpRegistrationRequest))

      server.stubFor(
        post(urlEqualTo(createRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(GATEWAY_TIMEOUT)
            .withFixedDelay(fixedDelay))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        whenReady(connector.createRegistration(etmpRegistrationRequest), Timeout(Span(timeOutSpan, Seconds))) { exp =>
          exp.isLeft mustBe true
          exp.left.toOption.get mustBe a[ErrorResponse]
        }
      }
    }
  }

  ".getRegistration" - {

    "should return an ETMP Display Registration response correctly" in {

      val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

      val responseJson: String = Json.toJson(etmpDisplayRegistration)(etmpDisplayRegistrationWrites).toString

      val app = application

      server.stubFor(
        get(urlEqualTo(getRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(responseJson))
      )

      running(application) {

        val connector = app.injector.instanceOf[RegistrationConnector]

        val result = connector.getRegistration(iossNumber).futureValue

        result `mustBe` Right(etmpDisplayRegistration)
      }
    }

    "should return Invalid Json when server responds with InvalidJson" in {

      val responseJson: String = Json.toJson("test" -> "invalid").toString

      val app = application

      server.stubFor(
        get(urlEqualTo(getRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(responseJson))
      )

      running(application) {

        val connector = app.injector.instanceOf[RegistrationConnector]

        val result = connector.getRegistration(iossNumber).futureValue

        result `mustBe` Left(InvalidJson)
      }
    }

    val listOfVariousErrors = Seq((NOT_FOUND, ServerError), (CONFLICT, ServerError), (INTERNAL_SERVER_ERROR, ServerError), (BAD_REQUEST, ServerError), (SERVICE_UNAVAILABLE, ServerError), (123, ServerError))

    listOfVariousErrors.foreach { error =>
      s"must return Left($error) when server responds with an error" in {

        val responseBody = ""

        val app = application

        server.stubFor(
          get(urlEqualTo(getRegistrationUrl))
            .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
            .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
            .willReturn(aResponse()
              .withStatus(error._1)
              .withBody(responseBody))
        )

        running(application) {

          val connector = app.injector.instanceOf[RegistrationConnector]

          val result = connector.getRegistration(iossNumber).futureValue

          result `mustBe` Left(error._2)
        }
      }
    }

    "must return Error Response when server responds with Http Exception" in {

      val app = application

      server.stubFor(
        get(urlEqualTo(getRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .willReturn(aResponse()
            .withStatus(GATEWAY_TIMEOUT)
            .withFixedDelay(fixedDelay))
      )

      running(app) {

        val connector = app.injector.instanceOf[RegistrationConnector]

        whenReady(connector.getRegistration(iossNumber), Timeout(Span(timeOutSpan, Seconds))) { exp =>
          exp.isLeft `mustBe` true
          exp.left.toOption.get `mustBe` a[ErrorResponse]
        }
      }
    }
  }
  ".amendRegistration" - {

    "should return AmendRegistrationResponse when server responds with 200 OK" in {
      val amendResponse = AmendRegistrationResponse(
        processingDateTime = LocalDateTime.now(),
        formBundleNumber = "123456789",
        iossReference = "IM900123456",
        businessPartner = "Test Business Partner"
      )

      val app = new GuiceApplicationBuilder()
        .configure(
          "microservice.services.amend-registration.host" -> "127.0.0.1",
          "microservice.services.amend-registration.port" -> server.port,
          "microservice.services.amend-registration.authorizationToken" -> "auth-token"
        )
        .build()

      val requestJson = Json.stringify(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(amendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse()
            .withStatus(OK)
            .withBody(Json.stringify(Json.toJson(amendResponse))))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(RegistrationData.etmpAmendRegistrationRequest).futureValue

        result mustBe Right(amendResponse)
      }
    }

    "should return NotFound when server responds with 404" in {
      val app = new GuiceApplicationBuilder()
        .configure(
          "microservice.services.amend-registration.host" -> "127.0.0.1",
          "microservice.services.amend-registration.port" -> server.port,
          "microservice.services.amend-registration.authorizationToken" -> "auth-token"
        )
        .build()

      val requestJson = Json.stringify(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(amendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(RegistrationData.etmpAmendRegistrationRequest).futureValue

        result mustBe Left(NotFound)
      }
    }

    "should return ServerError when server responds with 500" in {
      val app = new GuiceApplicationBuilder()
        .configure(
          "microservice.services.amend-registration.host" -> "127.0.0.1",
          "microservice.services.amend-registration.port" -> server.port,
          "microservice.services.amend-registration.authorizationToken" -> "auth-token"
        )
        .build()

      val requestJson = Json.stringify(Json.toJson(RegistrationData.etmpAmendRegistrationRequest))

      server.stubFor(
        put(urlEqualTo(amendRegistrationUrl))
          .withHeader(AUTHORIZATION, equalTo("Bearer auth-token"))
          .withHeader(CONTENT_TYPE, equalTo(MimeTypes.JSON))
          .withRequestBody(equalTo(requestJson))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]
        val result = connector.amendRegistration(RegistrationData.etmpAmendRegistrationRequest).futureValue

        result mustBe Left(ServerError)
      }
    }

  }
}
