/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.iossnetpregistration.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.config.AppConfig
import uk.gov.hmrc.iossnetpregistration.connectors.EnrolmentsConnector
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.RegistrationStatus
import uk.gov.hmrc.iossnetpregistration.models.audit.EtmpRegistrationAuditType.AmendRegistration
import uk.gov.hmrc.iossnetpregistration.models.audit.SubmissionResult.{Failure, Success}
import uk.gov.hmrc.iossnetpregistration.models.audit.{EtmpAmendRegistrationAuditModel, EtmpRegistrationAuditType, EtmpRegistrationRequestAuditModel, SubmissionResult}
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.{AmendRegistrationResponse, EtmpAmendRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse}
import uk.gov.hmrc.iossnetpregistration.models.etmp.{EtmpRegistrationRequest, EtmpRegistrationStatus}
import uk.gov.hmrc.iossnetpregistration.models.requests.ClientIdentifierRequest
import uk.gov.hmrc.iossnetpregistration.models.responses.{EtmpEnrolmentError, EtmpException}
import uk.gov.hmrc.iossnetpregistration.repositories.RegistrationStatusRepository
import uk.gov.hmrc.iossnetpregistration.services.{AuditService, RegistrationService, RetryService}
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class RegistrationController @Inject()(
                                             cc: AuthenticatedControllerComponents,
                                             enrolmentsConnector: EnrolmentsConnector,
                                             registrationService: RegistrationService,
                                             auditService: AuditService,
                                             registrationStatusRepository: RegistrationStatusRepository,
                                             retryService: RetryService,
                                             appConfig: AppConfig,
                                             clock: Clock
                                           )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def createRegistration(): Action[EtmpRegistrationRequest] = cc.clientIdentify.async(parse.json[EtmpRegistrationRequest]) {
    implicit request: ClientIdentifierRequest[EtmpRegistrationRequest] =>
      registrationService.createRegistration(request.body).flatMap {
        case Right(etmpEnrolmentResponse) =>
          enrollRegistration(etmpEnrolmentResponse.formBundleNumber).map { etmpRegistrationStatus =>
            auditRegistrationEvent(
              formBundleNumber = etmpEnrolmentResponse.formBundleNumber,
              etmpEnrolmentResponse = etmpEnrolmentResponse,
              etmpRegistrationStatus = etmpRegistrationStatus,
              successResponse = Created(Json.toJson(etmpEnrolmentResponse)))
          }
        case Left(EtmpEnrolmentError(EtmpEnrolmentErrorResponse.alreadyActiveSubscriptionErrorCode, body)) =>
          auditService.audit(EtmpRegistrationRequestAuditModel.build(
            EtmpRegistrationAuditType.CreateRegistration, request.body, None, None, Some(body), SubmissionResult.Duplicate)
          )
          logger.error(
            s"Business Partner already has an active IOSS Subscription for this regime with error code ${EtmpEnrolmentErrorResponse.alreadyActiveSubscriptionErrorCode}" +
              s"with message body $body"
          )
          Conflict(Json.toJson(
            s"Business Partner already has an active IOSS Subscription for this regime with error code ${EtmpEnrolmentErrorResponse.alreadyActiveSubscriptionErrorCode}" +
              s"with message body $body"
          )).toFuture
        case Left(error) =>
          auditService.audit(EtmpRegistrationRequestAuditModel.build(
            EtmpRegistrationAuditType.CreateRegistration, request.body, None, None, Some(error.body), SubmissionResult.Failure)
          )
          logger.error(s"Internal server error ${error.body}")
          InternalServerError(Json.toJson(s"Internal server error ${error.body}")).toFuture
      }
  }

  private def enrollRegistration(formBundleNumber: String)
                                (implicit hc: HeaderCarrier): Future[EtmpRegistrationStatus] = {
    (for {
      _ <- registrationStatusRepository.delete(formBundleNumber)
      _ <- registrationStatusRepository.insert(RegistrationStatus(subscriptionId = formBundleNumber,
        status = EtmpRegistrationStatus.Pending))
      enrolmentResponse <- enrolmentsConnector.confirmEnrolment(formBundleNumber)
    } yield {
      val enrolmentResponseStatus = enrolmentResponse.status
      enrolmentResponseStatus match {
        case NO_CONTENT =>
          retryService.getEtmpRegistrationStatus(appConfig.maxRetryCount, appConfig.delay, formBundleNumber)
        case status =>
          logger.error(s"Failed to add enrolment - $status with body ${enrolmentResponse.body}")
          throw EtmpException(s"Failed to add enrolment - ${enrolmentResponse.body}")
      }
    }).flatten
  }

  private def auditRegistrationEvent(formBundleNumber: String,
                                     etmpEnrolmentResponse: EtmpEnrolmentResponse,
                                     etmpRegistrationStatus: EtmpRegistrationStatus,
                                     successResponse: Result)
                                    (implicit hc: HeaderCarrier, request: ClientIdentifierRequest[EtmpRegistrationRequest]): Result = {
    etmpRegistrationStatus match {
      case EtmpRegistrationStatus.Success =>
        auditService.audit(EtmpRegistrationRequestAuditModel.build(
          EtmpRegistrationAuditType.CreateRegistration, request.body, Some(etmpEnrolmentResponse), None, None, SubmissionResult.Success)
        )
        logger.info("Successfully created registration and enrolment")
        successResponse
      case registrationStatus: EtmpRegistrationStatus =>
        logger.error(s"Failed to add enrolment, got registration status $registrationStatus")
        registrationStatusRepository.set(RegistrationStatus(subscriptionId = formBundleNumber, status = EtmpRegistrationStatus.Error))
        throw EtmpException(s"Failed to add enrolment, got registration status $registrationStatus")
    }
  }

  def displayRegistration(iossNumber: String): Action[AnyContent] = cc.auth().async {
    implicit request =>

      registrationService.getRegistration(iossNumber).map { registrationWrapper =>
        Ok(Json.toJson(registrationWrapper))
      }.recover {
        case exception =>
          logger.error(exception.getMessage, exception)
          InternalServerError(exception.getMessage)
      }
  }

  def amend(): Action[EtmpAmendRegistrationRequest] = cc.clientIdentify.async(parse.json[EtmpAmendRegistrationRequest]) {
    implicit request =>
      val etmpAmendRegistrationRequest = request.body

      registrationService.amendRegistration(request.body).map { amendResponse =>
        auditService.audit(
          EtmpAmendRegistrationAuditModel.build(
            etmpRegistrationAuditType = AmendRegistration,
            etmpRegistrationRequest = etmpAmendRegistrationRequest,
            etmpEnrolmentResponse = None,
            etmpAmendResponse = Some(amendResponse),
            errorResponse = None,
            submissionResult = Success
          )(request)
        )
        
        logger.info("Successfully amended registration")
        Ok(Json.toJson(amendResponse))
      }.recover {
        case error: EtmpException =>
          auditService.audit(
            EtmpAmendRegistrationAuditModel.build(
              etmpRegistrationAuditType = AmendRegistration,
              etmpRegistrationRequest = etmpAmendRegistrationRequest,
              etmpEnrolmentResponse = None,
              etmpAmendResponse = None,
              errorResponse = Some(error.message),
              submissionResult = Failure
            )
          )
          
          logger.error(s"Amend registration failed: ${error.getMessage}")
          InternalServerError(Json.toJson(s"Internal server error: ${error.getMessage}"))
          
        case error =>
          auditService.audit(
            EtmpAmendRegistrationAuditModel.build(
              etmpRegistrationAuditType = AmendRegistration,
              etmpRegistrationRequest = etmpAmendRegistrationRequest,
              etmpEnrolmentResponse = None,
              etmpAmendResponse = None,
              errorResponse = Some(error.getMessage),
              submissionResult = Failure
            )
          )

          logger.error(s"Unexpected error during amend registration: ${error.getMessage}")
          InternalServerError(Json.toJson(s"Internal server error: ${error.getMessage}"))
      }
  }
}
