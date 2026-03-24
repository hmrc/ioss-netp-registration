/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import play.api.http.Status.*
import play.api.libs.json.{JsError, JsSuccess, Reads}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.responses.*

trait BaseHttpParser extends Logging {

  val serviceName: String

  def parseResponse[T](response: HttpResponse)(implicit rds: Reads[T]): Either[ErrorResponse, T] = {
    response.status match {
      case OK => response.json.validate[T] match {
        case JsSuccess(registration, _) => Right(registration)
        case JsError(errors) =>
          logger.error(s"Failed trying to parse JSON with errors: $errors")
          Left(InvalidJson)
      }
      case NOT_FOUND =>
        logger.warn(s"Received NotFound from $serviceName")
        Left(NotFound)
      case INTERNAL_SERVER_ERROR =>
        logger.error(s"Received InternalServerError from $serviceName")
        Left(ServerError)
      case BAD_REQUEST =>
        logger.error(s"Received BadRequest from $serviceName")
        Left(InvalidVrn)
      case SERVICE_UNAVAILABLE =>
        logger.error(s"Received Service Unavailable from $serviceName")
        Left(ServiceUnavailable)
      case CONFLICT =>
        logger.error(s"Received Conflict from $serviceName")
        Left(Conflict)
      case status =>
        logger.error(s"Unexpected response from $serviceName, received status $status with body ${response.body}")
        Left(UnexpectedResponseStatus(status, s"Unexpected response from $serviceName, received status $status with body ${response.body}"))
    }
  }
}
