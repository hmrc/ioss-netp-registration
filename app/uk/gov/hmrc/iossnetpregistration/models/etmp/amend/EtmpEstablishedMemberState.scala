/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

case class EtmpEstablishedMemberState(fixedEstIssuedBy: Option[String],
                                      fixedEstVATNumber: Option[String])

object EtmpEstablishedMemberState {

  implicit val format: OFormat[EtmpEstablishedMemberState] = Json.format[EtmpEstablishedMemberState]

}