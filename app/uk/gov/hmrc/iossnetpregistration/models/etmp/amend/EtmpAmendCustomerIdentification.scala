/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

case class EtmpAmendCustomerIdentification(iossNumber: String, foreignTaxReference: Option[String])

object EtmpAmendCustomerIdentification {
  implicit val format: OFormat[EtmpAmendCustomerIdentification] = Json.format[EtmpAmendCustomerIdentification]
}