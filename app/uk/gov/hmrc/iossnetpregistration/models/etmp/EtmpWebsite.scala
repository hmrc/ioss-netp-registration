/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpWebsite(websiteAddress: String)
object EtmpWebsite {

  implicit val format: OFormat[EtmpWebsite] = Json.format[EtmpWebsite]
}
