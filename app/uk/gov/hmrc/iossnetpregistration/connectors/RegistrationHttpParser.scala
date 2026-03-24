/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.http.Status.{CREATED, NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.{EtmpEnrolmentErrorResponse, EtmpEnrolmentResponse}
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.AmendRegistrationResponse
import uk.gov.hmrc.iossnetpregistration.models.responses.*

object RegistrationHttpParser extends BaseHttpParser {

  override val serviceName: String = "etmp registration"

  type CreateEtmpRegistrationResponse = Either[ErrorResponse, EtmpEnrolmentResponse]
  type EtmpDisplayRegistrationResponse = Either[ErrorResponse, EtmpDisplayRegistration]

  type CreateAmendRegistrationResponse = Either[ErrorResponse, AmendRegistrationResponse]

  implicit object CreateRegistrationReads extends HttpReads[CreateEtmpRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): CreateEtmpRegistrationResponse =
      response.status match {
        case CREATED => response.json.validate[EtmpEnrolmentResponse] match {
          case JsSuccess(enrolmentResponse, _) => Right(enrolmentResponse)
          case JsError(errors) =>
            logger.error(s"Failed trying to parse JSON, but was successfully created ${response.body} $errors")
            Left(InvalidJson)
        }
        case status =>
          if (response.body.nonEmpty) {
            response.json.validate[EtmpEnrolmentErrorResponse] match {
              case JsSuccess(enrolmentErrorResponse, _) =>
                Left(EtmpEnrolmentError(
                  code = enrolmentErrorResponse.errorDetail.errorCode.getOrElse("No error code"),
                  body = enrolmentErrorResponse.errorDetail.errorMessage.getOrElse("No error message")
                ))
              case JsError(errors) =>
                logger.error(s"Failed trying to parse JSON with status $status and body ${response.body} json parse error: $errors")
                Left(UnexpectedResponseStatus(status, s"Unexpected response from ${serviceName}, received status $status"))
            }
          } else {
            logger.error(s"Failed trying to parse empty JSON with status ${response.status} and body ${response.body}")
            logger.warn(s"Unexpected response from core registration, received status $status")
            Left(UnexpectedResponseStatus(status, s": Unexpected response from ${serviceName}, received status $status"))
          }
      }
  }

  implicit object CreateAmendRegistrationResponseReads extends HttpReads[CreateAmendRegistrationResponse] {
      override def read(method: String, url: String, response: HttpResponse): CreateAmendRegistrationResponse =
        response.status match {
          case OK => response.json.validate[AmendRegistrationResponse] match {
            case JsSuccess(amendRegistrationResponse, _) => Right(amendRegistrationResponse)
            case JsError(errors) =>
              logger.error(s"Failed trying to parse JSON with status ${response.status} and body ${response.body} errors ${errors}")
              Left(InvalidJson)
          }
          case NOT_FOUND =>
            logger.warn(s"url not reachable")
            Left(NotFound)
          case status =>
            logger.error(s"Unknown error happened on amend registration $status with body ${response.body}")
            Left(ServerError)
        }
  }
  implicit object EtmpDisplayRegistrationReads extends HttpReads[EtmpDisplayRegistrationResponse] {

    override def read(method: String, url: String, response: HttpResponse): EtmpDisplayRegistrationResponse = {
      response.status match {
        case OK =>
          response.json.validate[EtmpDisplayRegistration] match
            case JsSuccess(etmpDisplayRegistrationResponse, _) => Right(etmpDisplayRegistrationResponse)
            case JsError(errors) =>
              logger.error(s"Failed trying to parse EtmpDisplayRegistration response JSON with response status: ${response.status}, with errors: $errors")
              Left(InvalidJson)

        case status =>
          logger.error(s"An unexpected error occurred when trying to retrieve EtmpDisplayRegistration with status: $status and response body: ${response.body}")
          Left(ServerError)
      }
    }
  }
}
