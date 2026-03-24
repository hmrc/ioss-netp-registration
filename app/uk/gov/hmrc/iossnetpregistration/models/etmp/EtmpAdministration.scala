/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpAdministration(messageType: EtmpMessageType, regimeID: String = "IOSS")

object EtmpAdministration {

  implicit val format: OFormat[EtmpAdministration] = Json.format[EtmpAdministration]

}