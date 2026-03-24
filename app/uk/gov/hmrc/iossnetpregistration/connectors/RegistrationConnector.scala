/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.http.HeaderNames.*
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpException, StringContextOps}
import uk.gov.hmrc.iossnetpregistration.config.{AmendRegistrationConfig, CreateRegistrationConfig, EtmpDisplayRegistrationConfig}
import uk.gov.hmrc.iossnetpregistration.connectors.RegistrationHttpParser.*
import uk.gov.hmrc.iossnetpregistration.connectors.VatCustomerInfoHttpParser.*
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpRegistrationRequest
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.*
import uk.gov.hmrc.iossnetpregistration.models.responses.UnexpectedResponseStatus

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class RegistrationConnector @Inject()(
                                            httpClientV2: HttpClientV2,
                                            createRegistrationConfig: CreateRegistrationConfig,
                                            amendRegistrationConfig: AmendRegistrationConfig,
                                            etmpDisplayRegistrationConfig: EtmpDisplayRegistrationConfig,
                                          )(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def createHeaders(correlationId: String): Seq[(String, String)] = createRegistrationConfig.eisEtmpCreateHeaders(correlationId)
  private def amendHeaders(correlationId: String): Seq[(String, String)] = amendRegistrationConfig.eisEtmpAmendHeaders(correlationId)
  private def getHeaders(correlationId: String): Seq[(String, String)] = etmpDisplayRegistrationConfig.eisEtmpGetHeaders(correlationId)

  def createRegistration(registration: EtmpRegistrationRequest): Future[CreateEtmpRegistrationResponse] = {

    val correlationId = UUID.randomUUID.toString
    val headersWithCorrelationId = createHeaders(correlationId)
    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending create request to etmp with headers $headersWithoutAuth")

    httpClientV2.post(url"${createRegistrationConfig.baseUrl}vec/iosssubscription/subdatatransfer/v1")
      .withBody(Json.toJson(registration))
      .setHeader(headersWithCorrelationId: _*)
      .execute[CreateEtmpRegistrationResponse].recover {
        case e: HttpException =>
          logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
          Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${RegistrationHttpParser.serviceName}, received status ${e.responseCode}"))
      }
  }

  def amendRegistration(registration: EtmpAmendRegistrationRequest): Future[CreateAmendRegistrationResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val correlationId = UUID.randomUUID.toString
    val headersWithCorrelationId = amendHeaders(correlationId)
    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending amend request to etmp with headers $headersWithoutAuth")

    httpClientV2.put(url"${amendRegistrationConfig.baseUrl}vec/iossregistration/amendregistration/v1")
      .withBody(Json.toJson(registration))
      .setHeader(headersWithCorrelationId: _*)
      .execute[CreateAmendRegistrationResponse]
      .recover {
        case e: HttpException =>
          logger.error(s"Unexpected response from etmp registration ${e.getMessage}", e)
          Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ${RegistrationHttpParser.serviceName}, received status ${e.responseCode}"))
      }

  }

  def getRegistration(iossNumber: String): Future[EtmpDisplayRegistrationResponse] = {

    val correlationId: String = UUID.randomUUID.toString
    val headersWithCorrelationId = getHeaders(correlationId)

    httpClientV2.get(url"${etmpDisplayRegistrationConfig.baseUrl}vec/iossregistration/viewreg/v1/$iossNumber")
      .setHeader(headersWithCorrelationId: _*)
      .execute[EtmpDisplayRegistrationResponse].recover {
        case e: HttpException =>
          logger.error(s"Unexpected response from ETMP Display Registration ${e.message}", e)
          Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response from ETMP Display Registration with status ${e.responseCode}"))
      }
  }
}
