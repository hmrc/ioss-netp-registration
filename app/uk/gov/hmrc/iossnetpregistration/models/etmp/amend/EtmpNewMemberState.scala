/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpNewMemberState(newMemberState: Boolean,
                              ceaseSpecialSchemeDate: Option[LocalDate],
                              ceaseFixedEstDate: Option[LocalDate],
                              movePOBDate: Option[LocalDate],
                              issuedBy: Option[String],
                              vatNumber: Option[String])


object EtmpNewMemberState {

  implicit val format: OFormat[EtmpNewMemberState] = Json.format[EtmpNewMemberState]

}
