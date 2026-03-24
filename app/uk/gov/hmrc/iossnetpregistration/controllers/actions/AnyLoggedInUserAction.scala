/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers.actions

import com.google.inject.Inject
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.requests.{AnyLoggedInUserRequest, IdentifierRequest}
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait AnyLoggedInUserAction extends ActionBuilder[AnyLoggedInUserRequest, AnyContent] with ActionFunction[Request, AnyLoggedInUserRequest]

class AnyLoggedInUserActionImpl @Inject()(
                                           override val authConnector: AuthConnector,
                                           val parser: BodyParsers.Default
                                         )
                                         (implicit val executionContext: ExecutionContext) extends AnyLoggedInUserAction with AuthorisedFunctions with Logging {


  override def invokeBlock[A](request: Request[A], block: AnyLoggedInUserRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(internalId) ~ enrolments =>
        block(AnyLoggedInUserRequest(request, internalId, enrolments))
      case _ =>
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case e: NoActiveSession =>
        Unauthorized(e.getMessage)
      case e: AuthorisationException =>
        Unauthorized(e.getMessage)
    }
  }

}

