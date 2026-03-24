/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.responses

import uk.gov.hmrc.iossnetpregistration.models.core.EisErrorResponse

sealed trait ErrorResponse {
  val body: String
}

case object InvalidVrn extends ErrorResponse {
  override val body: String = "Invalid VRN"
}

case object InvalidJson extends ErrorResponse {
  override val body: String = "Invalid Response"
}

case object NotFound extends ErrorResponse {
  override val body = "Not found"
}

case object Conflict extends ErrorResponse {
  override val body = "Conflict"
}

case object ServerError extends ErrorResponse {
  override val body = "Internal server error"
}

case object ServiceUnavailable extends ErrorResponse {
  override val body: String = "Service unavailable"
}

case object GatewayTimeout extends ErrorResponse {
  override val body: String = "Gateway timeout"
}

case class UnexpectedResponseStatus(status: Int, body: String) extends ErrorResponse

case class EisError(eisErrorResponse: EisErrorResponse) extends ErrorResponse {
  override val body: String =
    s"${eisErrorResponse.timestamp} " +
      s"${eisErrorResponse.error} " +
      s"${eisErrorResponse.errorMessage} "
}

case class EtmpEnrolmentError(code: String, body: String) extends ErrorResponse

case class EtmpException(message: String) extends Exception(message)



