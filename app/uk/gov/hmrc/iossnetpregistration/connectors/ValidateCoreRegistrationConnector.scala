/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpException, StringContextOps}
import uk.gov.hmrc.iossnetpregistration.config.CoreValidationConfig
import uk.gov.hmrc.iossnetpregistration.connectors.ValidateCoreRegistrationHttpParser.{ValidateCoreRegistrationReads, ValidateCoreRegistrationResponse}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.core.{CoreRegistrationRequest, EisErrorResponse}
import uk.gov.hmrc.iossnetpregistration.models.responses.EisError

import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateCoreRegistrationConnector @Inject()(
                                                   coreValidationConfig: CoreValidationConfig,
                                                   httpClientV2: HttpClientV2
                                                 )(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {
  
  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  
  private val baseUrl = coreValidationConfig.coreValidationUrl
  
  private def headers(correlationId: String): Seq[(String, String)] = coreValidationConfig.eisCoreHeaders(correlationId)
  
  def validateCoreRegistration(coreRegistrationRequest: CoreRegistrationRequest): Future[ValidateCoreRegistrationResponse] = {

    val correlationId: String = UUID.randomUUID.toString
    val headersWithCorrelationId = headers(correlationId)

    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending request to EIS with headers $headersWithoutAuth")
    val url = url"$baseUrl"
    httpClientV2.post(url)
      .withBody(Json.toJson(coreRegistrationRequest))
      .setHeader(headersWithCorrelationId: _*)
      .execute[ValidateCoreRegistrationResponse]
      .recover {
        case e: HttpException =>
          val selfGeneratedRandomUUID = UUID.randomUUID()
          logger.error(
            s"Unexpected error response from EIS $url, received status ${e.responseCode}," +
              s"body of response was: ${e.message} with self-generated CorrelationId $selfGeneratedRandomUUID " +
              s"and original correlation ID we tried to pass $correlationId"
          )
          Left(EisError(
            EisErrorResponse(Instant.now(), s"UNEXPECTED_${e.responseCode.toString}", e.message)
          ))
      }
  }

}
