/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.des

import play.api.libs.json.*

sealed trait PartyType

object PartyType {

  case object VatGroup extends PartyType

  case object OtherPartyType extends PartyType

  implicit val reads: Reads[PartyType] = new Reads[PartyType] {

    override def reads(json: JsValue): JsResult[PartyType] =
      json match {
        case JsString("Z2") => JsSuccess(VatGroup)
        case _              => JsSuccess(OtherPartyType)
      }
  }
}
