package uk.gov.hmrc.iossnetpregistration.connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.Application
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class EnrolmentsConnectorSpec extends BaseSpec with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val errorResponseBody = "Error"

  ".confirmEnrolment" - {

    val basePath = "tax-enrolments/"

    def application: Application =
      new GuiceApplicationBuilder()
        .configure(
          "microservice.services.enrolments.host" -> "127.0.0.1",
          "microservice.services.enrolments.port" -> server.port,
          "microservice.services.enrolments.authorizationToken" -> "auth-token",
          "microservice.services.enrolments.basePath" -> basePath
        )
        .build()

    val subscriptionId = "123456789"
    val url = s"/${basePath}subscriptions/$subscriptionId/subscriber"

    "must return an HttpResponse with status NoContent when the server returns NoContent" in {

      val app = application

      server.stubFor(
        put(urlEqualTo(url))
          .willReturn(aResponse().withStatus(NO_CONTENT))
      )

      running(app) {
        val connector = app.injector.instanceOf[EnrolmentsConnector]
        val result = connector.confirmEnrolment(subscriptionId).futureValue

        result.status mustEqual NO_CONTENT
      }
    }


    Seq(BAD_REQUEST, UNAUTHORIZED).foreach {
      status =>
        s"must return an Http response with $status when the server returns $status" in {

          val app = application

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(aResponse().withStatus(status))
          )

          running(app) {
            val connector = app.injector.instanceOf[EnrolmentsConnector]

            val result = connector.confirmEnrolment(subscriptionId).futureValue

            result.status mustEqual status
          }
        }
    }

  }

}
