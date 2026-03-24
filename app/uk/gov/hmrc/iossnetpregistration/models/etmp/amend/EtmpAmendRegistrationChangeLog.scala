/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

case class EtmpAmendRegistrationChangeLog(
                                           tradingNames: Boolean,
                                           fixedEstablishments: Boolean,
                                           contactDetails: Boolean,
                                           bankDetails: Boolean,
                                           reRegistration: Boolean,
                                           otherAddress: Boolean)

object EtmpAmendRegistrationChangeLog {

  implicit val format: OFormat[EtmpAmendRegistrationChangeLog] = Json.format[EtmpAmendRegistrationChangeLog]

}
