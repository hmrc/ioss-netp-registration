package uk.gov.hmrc.iossnetpregistration.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, post, urlEqualTo}
import org.scalacheck.Gen
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, METHOD_NOT_ALLOWED, NOT_ACCEPTABLE, NOT_FOUND, SERVICE_UNAVAILABLE, UNSUPPORTED_MEDIA_TYPE, running}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.core.*
import uk.gov.hmrc.iossnetpregistration.models.responses.{EisError, UnexpectedResponseStatus}
import uk.gov.hmrc.iossnetpregistration.testutils.SourceType

import java.time.{Instant, LocalDate}

class ValidateCoreRegistrationConnectorSpec extends BaseSpec with WireMockHelper {

  private val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

  private val timestamp = Instant.now

  def getValidateCoreRegistrationUrl = s"/ioss-netp-registration-stub/vec/iossregistration/iossregvalidation/v1"

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.core-validation.port" -> server.port)
      .configure("microservice.services.core-validation.authorizationToken" -> "auth-token")
      .build()

  private val validCoreRegistrationResponse: CoreRegistrationValidationResult =
    CoreRegistrationValidationResult(
      searchId = "12345678",
      searchIdIntermediary = Some("12345678"),
      searchIdIssuedBy = "FR",
      traderFound = true,
      matches = Seq(
        Match(
          traderId = "IN12345678",
          intermediary = Some("IN4819283759"),
          memberState = "DE",
          exclusionStatusCode = Some(3),
          exclusionDecisionDate = Some(LocalDate.now().format(Match.dateFormatter)),
          exclusionEffectiveDate = Some(LocalDate.now().format(Match.dateFormatter)),
          nonCompliantReturns = None,
          nonCompliantPayments = None
        )
      )
    )

  "validateCoreRegistration" - {

    "must return Right(CoreRegistrationValidationResult) when the server returns OK for a recognised payload" in {

      val responseJson = Json.prettyPrint(Json.toJson(validCoreRegistrationResponse))

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(ok(responseJson))
      )

      running(application) {
        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        result mustBe Right(validCoreRegistrationResponse)
      }
    }

    "must return an expected response when the server returns a parsable error" in {

      val status = Gen.oneOf(
        BAD_REQUEST,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE
      ).sample.value

      val errorResponseJson =
        s"""{
           |  "timestamp": "$timestamp",
           |  "error": "$status",
           |  "errorMessage": "Error"
           |}""".stripMargin


      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(aResponse()
            .withStatus(status)
            .withBody(errorResponseJson)
          )
      )

      running(application) {

        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        val expectedResponse = EisError(EisErrorResponse(timestamp, s"$status", "Error"))

        result mustBe Left(expectedResponse)

      }
    }

    "must return an expected response when the server returns with an empty body" in {

      val status = Gen.oneOf(
        BAD_REQUEST,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE
      ).sample.value

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(aResponse()
            .withStatus(status))
      )

      running(application) {

        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        val errorResponse = result.left.toOption.get.asInstanceOf[EisError].eisErrorResponse

        val expectedResponse = EisError(
          EisErrorResponse(
            errorResponse.timestamp,
            s"$status",
            "The response body was empty"
          )
        )

        result mustBe Left(expectedResponse)
      }
    }

    "must return Left(UnexpectedStatus) when the server returns another error code" in {

      val status = Gen.oneOf(
        BAD_REQUEST,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE
      ).sample.value

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(aResponse()
            .withStatus(status)
            .withBody("{}"))
      )

      running(application) {
        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        result mustBe Left(
          UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
      }
    }

    "must return an Eis Error when the server returns an Http Exception" in {

      val timeout = 30

      val status = Gen.oneOf(
        BAD_REQUEST,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE
      ).sample.value

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(aResponse()
            .withStatus(status))
      )

      running(application) {

        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        whenReady(connector.validateCoreRegistration(coreRegistrationRequest), Timeout(Span(timeout, Seconds))) {
          exp =>
            exp.isLeft mustBe true
            exp.left.toOption.get mustBe a[EisError]
        }
      }
    }

  }

}
