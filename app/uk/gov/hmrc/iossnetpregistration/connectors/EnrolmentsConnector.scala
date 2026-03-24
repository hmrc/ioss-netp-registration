/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.iossnetpregistration.config.TaxEnrolmentsConfig
import uk.gov.hmrc.iossnetpregistration.controllers.routes
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.enrolments.SubscriberRequest

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentsConnector @Inject()(
                                     taxEnrolmentsConfig: TaxEnrolmentsConfig,
                                     httpClientV2: HttpClientV2
                                   )
                                   (implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  def confirmEnrolment(subscriptionId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val etmpId = UUID.randomUUID.toString

    httpClientV2.put(url"${taxEnrolmentsConfig.baseUrl}subscriptions/$subscriptionId/subscriber")
      .withBody(Json.toJson(
        SubscriberRequest(taxEnrolmentsConfig.iossNetpEnrolmentName,
          s"${taxEnrolmentsConfig.callbackBaseUrl}${routes.EnrolmentsSubscriptionController.authoriseEnrolment(subscriptionId).url}",
          etmpId
        )
      )).execute[HttpResponse]
  }

}
