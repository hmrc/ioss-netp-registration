/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class EtmpAdminUse(changeDate: Option[LocalDateTime])

object EtmpAdminUse {

  implicit val format: OFormat[EtmpAdminUse] = Json.format[EtmpAdminUse]
}