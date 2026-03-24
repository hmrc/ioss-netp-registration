/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.http.HeaderNames
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier, HttpErrorFunctions, StringContextOps}
import uk.gov.hmrc.iossnetpregistration.config.GetVatInfoConfig
import uk.gov.hmrc.iossnetpregistration.connectors.VatCustomerInfoHttpParser.{VatCustomerInfoReads, VatCustomerInfoResponse}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.responses.GatewayTimeout

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GetVatInfoConnector @Inject()(
                                     getVatInfoConfig: GetVatInfoConfig,
                                     httpClientV2: HttpClientV2
                                   )(implicit ec: ExecutionContext)
  extends HttpErrorFunctions with Logging {

  private val XCorrelationId = "X-Correlation-Id"

  private def headers(correlationId: String): Seq[(String, String)] = Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer ${getVatInfoConfig.authorizationToken}",
    "Environment" -> getVatInfoConfig.environment,
    XCorrelationId -> correlationId
  )

  def getVatCustomerDetails(vrn: Vrn)(implicit headerCarrier: HeaderCarrier): Future[VatCustomerInfoResponse] = {
    httpClientV2
      .get(url"${getVatInfoConfig.baseUrl}vat/customer/vrn/${vrn.value}/information")
      .setHeader(headers(UUID.randomUUID.toString): _*)
      .execute[VatCustomerInfoResponse]
      .recover {
        case e: GatewayTimeoutException =>
          logger.error(s"Request timeout from Get vat info: $e", e)
          Left(GatewayTimeout)
      }
  }
}
