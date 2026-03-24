/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{Json, OFormat}

case class Country(code: String, name: String)

object Country {

  implicit val format: OFormat[Country] = Json.format[Country]
}
