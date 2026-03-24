/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class EtmpAmendCustomerIdentificationSpec extends BaseSpec with Matchers {

  private val iossNumber = "IN900123456"
  private val foreignTaxReference = "BR123456789"

  "EtmpAmendCustomerIdentification" - {

    "must deserialise/serialise to and from EtmpAmendCustomerIdentification when only required fields are present" in {

      val json = Json.obj(
        "iossNumber" -> iossNumber
      )

      val expectedResult = EtmpAmendCustomerIdentification(
        iossNumber = iossNumber,
        foreignTaxReference = None
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpAmendCustomerIdentification] mustBe JsSuccess(expectedResult)
    }

    "must deserialise/serialise to and from EtmpAmendCustomerIdentification when all fields are present" in {

      val json = Json.obj(
        "iossNumber" -> iossNumber,
        "foreignTaxReference" -> foreignTaxReference
      )

      val expectedResult = EtmpAmendCustomerIdentification(
        iossNumber = iossNumber,
        foreignTaxReference = Some(foreignTaxReference)
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpAmendCustomerIdentification] mustBe JsSuccess(expectedResult)
    }
  }
}