/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp

import play.api.libs.json.{Json, OFormat}

case class EtmpTradingName(tradingName: String)

object EtmpTradingName {

  implicit val format: OFormat[EtmpTradingName] = Json.format[EtmpTradingName]
}
