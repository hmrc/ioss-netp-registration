/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.enrolments

import play.api.libs.json.*

sealed trait EnrolmentStatus

object EnrolmentStatus {

  case object Success extends EnrolmentStatus {
    val jsonName = "SUCCEEDED"
  }

  case object Failure extends EnrolmentStatus {
    val jsonName = "ERROR"
  }

  case object Enrolled extends EnrolmentStatus {
    val jsonName = "Enrolled"
  }

  case object EnrolmentError extends EnrolmentStatus {
    val jsonName = "EnrolmentError"
  }

  case object AuthRefreshed extends EnrolmentStatus {
    val jsonName = "AuthRefreshed"
  }

  implicit object EnrolmentStatusJsonReads extends Reads[EnrolmentStatus] {

    override def reads(json: JsValue): JsResult[EnrolmentStatus] =
      json.validate[String].flatMap {
        case Success.jsonName => JsSuccess(Success)
        case Failure.jsonName | Enrolled.jsonName | EnrolmentError.jsonName |
             AuthRefreshed.jsonName =>
          JsSuccess(Failure)
        case e =>
          JsError(s"Unable to parse json $e")
      }

  }
}