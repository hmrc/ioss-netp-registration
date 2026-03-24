/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.display

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo

case class RegistrationWrapper(vatInfo: Option[VatCustomerInfo], etmpDisplayRegistration: EtmpDisplayRegistration)

object RegistrationWrapper {

  implicit val format: OFormat[RegistrationWrapper] = Json.format[RegistrationWrapper]
}
