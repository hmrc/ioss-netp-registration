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
import uk.gov.hmrc.iossnetpregistration.models.requests.IdentifierRequest
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: AppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  
  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    
    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(internalId) ~ enrolments =>
        findIntermediaryNumberFromEnrolments(enrolments) match {
          case Some(intermediaryNumber) =>
            val vrn = findVrnFromEnrolments(enrolments)
            block(IdentifierRequest(request, internalId, enrolments, vrn, intermediaryNumber))
          case None =>
            logger.warn(s"Not an intermediary attempted to access this endpoint ${request.uri}")
            Forbidden("Not an intermediary").toFuture
        }
      case _ =>
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case e: NoActiveSession =>
        Unauthorized(e.getMessage)
      case e: AuthorisationException =>
        Unauthorized(e.getMessage)
    }
  }

  private def findVrnFromEnrolments(enrolments: Enrolments): Vrn = {
    enrolments.enrolments.find(_.key == "HMRC-MTD-VAT")
      .flatMap(_.identifiers.find(id => id.key == "VRN").map(e => Vrn(e.value)))
      .getOrElse {
        logger.warn("User does not have a valid VAT enrolment")
        throw new IllegalStateException("Missing VAT enrolment")
      }
  }
  
  private def findIntermediaryNumberFromEnrolments(enrolments: Enrolments): Option[String] = {
    enrolments.enrolments
      .find(_.key == config.intermediaryEnrolmentName)
      .flatMap(_.identifiers.find(id => id.key == config.intermediaryEnrolmentKey && id.value.nonEmpty).map(_.value))
  }

  private def findNetpFromEnrolments(enrolments: Enrolments): Boolean = {
    enrolments.enrolments.exists(_.key == "HMRC-IOSS-NETP")
  }
}

