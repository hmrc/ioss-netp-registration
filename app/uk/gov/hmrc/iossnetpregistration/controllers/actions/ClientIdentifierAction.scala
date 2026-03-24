/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers.actions

import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.requests.ClientIdentifierRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait ClientIdentifierAction extends ActionBuilder[ClientIdentifierRequest, AnyContent] with ActionFunction[Request, ClientIdentifierRequest]

class ClientIdentifierActionImpl @Inject()(
                                            override val authConnector: AuthConnector,
                                            val parser: BodyParsers.Default
                                          )(implicit val ec: ExecutionContext)
  extends ClientIdentifierAction with AuthorisedFunctions with Logging {

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](
                               request: Request[A],
                               block: ClientIdentifierRequest[A] => Future[Result]
                             ): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(internalId) ~ enrolments =>
        block(ClientIdentifierRequest(request, internalId, enrolments))
      case _ =>
        logger.error(s"No Internal ID found to create User ID. Request Body:${request.body}")
        Future.failed(new IllegalStateException("Missing Internal ID"))
    }
  }
}
